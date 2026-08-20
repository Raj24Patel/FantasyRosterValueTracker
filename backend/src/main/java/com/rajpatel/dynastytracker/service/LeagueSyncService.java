package com.rajpatel.dynastytracker.service;

import com.rajpatel.dynastytracker.domain.League;
import com.rajpatel.dynastytracker.domain.Manager;
import com.rajpatel.dynastytracker.domain.Player;
import com.rajpatel.dynastytracker.domain.Roster;
import com.rajpatel.dynastytracker.domain.RosterPlayer;
import com.rajpatel.dynastytracker.domain.SyncLog;
import com.rajpatel.dynastytracker.repository.LeagueRepository;
import com.rajpatel.dynastytracker.repository.ManagerRepository;
import com.rajpatel.dynastytracker.repository.PlayerRepository;
import com.rajpatel.dynastytracker.repository.RosterRepository;
import com.rajpatel.dynastytracker.repository.SyncLogRepository;
import com.rajpatel.dynastytracker.sleeper.SleeperClient;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperLeague;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperRoster;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperUser;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Orchestrates a league sync: calls Sleeper for league/user/roster data,
 * refreshes the player catalog if it's stale, writes everything in one
 * transaction, then captures that day's value snapshots. Records every
 * attempt in a {@link SyncLog} row.
 */
@Service
public class LeagueSyncService {

    private static final Logger log = LoggerFactory.getLogger(LeagueSyncService.class);

    private final SleeperClient sleeperClient;
    private final LeagueRepository leagueRepository;
    private final ManagerRepository managerRepository;
    private final RosterRepository rosterRepository;
    private final PlayerRepository playerRepository;
    private final SyncLogRepository syncLogRepository;
    private final PlayerCatalogService playerCatalogService;
    private final SnapshotService snapshotService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final int maxLeagues;

    public LeagueSyncService(SleeperClient sleeperClient,
                             LeagueRepository leagueRepository,
                             ManagerRepository managerRepository,
                             RosterRepository rosterRepository,
                             PlayerRepository playerRepository,
                             SyncLogRepository syncLogRepository,
                             PlayerCatalogService playerCatalogService,
                             SnapshotService snapshotService,
                             TransactionTemplate transactionTemplate,
                             Clock clock,
                             @Value("${tracking.max-leagues:25}") int maxLeagues) {
        this.sleeperClient = sleeperClient;
        this.leagueRepository = leagueRepository;
        this.managerRepository = managerRepository;
        this.rosterRepository = rosterRepository;
        this.playerRepository = playerRepository;
        this.syncLogRepository = syncLogRepository;
        this.playerCatalogService = playerCatalogService;
        this.snapshotService = snapshotService;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
        this.maxLeagues = maxLeagues;
    }

    /**
     * Starts tracking a new league by running its first sync synchronously.
     * Re-adding a league that's already tracked is allowed (it just re-syncs)
     * and doesn't count against the cap.
     * @param sleeperLeagueId the Sleeper league ID to track
     * @return the newly persisted league
     * @throws LeagueLimitReachedException if the tracker is already at its league cap
     * @throws com.rajpatel.dynastytracker.sleeper.SleeperApiException if the first sync fails
     */
    public League addLeague(String sleeperLeagueId) {
        // checked before the sync so we don't spend three Sleeper calls to then refuse
        if (!leagueRepository.existsById(sleeperLeagueId) && leagueRepository.count() >= maxLeagues) {
            throw new LeagueLimitReachedException(maxLeagues);
        }
        sync(sleeperLeagueId);
        return leagueRepository.findById(sleeperLeagueId)
                .orElseThrow(() -> new LeagueNotFoundException(sleeperLeagueId));
    }

    /**
     * Fire-and-forget re-sync for an already-tracked league (used by the
     * manual "Re-sync" button). Failures are logged, not thrown, since
     * there's no caller left to catch them.
     * @param leagueId the league to re-sync
     */
    @Async
    public void syncAsync(String leagueId) {
        try {
            sync(leagueId);
        } catch (RuntimeException e) {
            log.error("Async sync of league {} failed: {}", leagueId, e.getMessage());
        }
    }

    /**
     * Runs one full sync for a league: fetch from Sleeper, persist, snapshot.
     * Everything is fetched into memory before anything is written, and the write
     * happens in one transaction. A failed sync must never delete existing data —
     * if Sleeper 500s halfway, yesterday's rosters are still there.
     * @param leagueId the Sleeper league ID to sync
     * @throws com.rajpatel.dynastytracker.sleeper.SleeperApiException if any Sleeper call fails after retries
     */
    public void sync(String leagueId) {
        SyncLog syncLog = syncLogRepository.save(new SyncLog(leagueId, OffsetDateTime.now(clock)));
        try {
            SleeperLeague sleeperLeague = sleeperClient.getLeague(leagueId);
            List<SleeperUser> users = sleeperClient.getUsers(leagueId);
            List<SleeperRoster> rosters = sleeperClient.getRosters(leagueId);
            playerCatalogService.refreshIfStale();

            transactionTemplate.executeWithoutResult(tx -> persist(sleeperLeague, users, rosters));
            snapshotService.captureSnapshots(leagueId, LocalDate.now(clock));

            syncLog.setStatus(SyncLog.Status.SUCCESS);
            syncLog.setFinishedAt(OffsetDateTime.now(clock));
            syncLogRepository.save(syncLog);
            log.info("Synced league {} ({} rosters)", leagueId, rosters.size());
        } catch (RuntimeException e) {
            syncLog.setStatus(SyncLog.Status.FAILED);
            syncLog.setMessage(e.getMessage());
            syncLog.setFinishedAt(OffsetDateTime.now(clock));
            syncLogRepository.save(syncLog);
            throw e;
        }
    }

    /**
     * Upserts the league, its managers, and its rosters (including per-roster
     * players) from freshly fetched Sleeper data. Called inside one transaction.
     * @param sleeperLeague league header data from Sleeper
     * @param users managers from Sleeper
     * @param sleeperRosters rosters from Sleeper
     */
    private void persist(SleeperLeague sleeperLeague, List<SleeperUser> users, List<SleeperRoster> sleeperRosters) {
        League league = leagueRepository.findById(sleeperLeague.leagueId()).orElseGet(League::new);
        league.setId(sleeperLeague.leagueId());
        league.setName(sleeperLeague.name());
        league.setSeason(sleeperLeague.season());
        league.setTotalRosters(sleeperLeague.totalRosters() != null ? sleeperLeague.totalRosters() : sleeperRosters.size());
        league.setSuperflex(sleeperLeague.isSuperflex());
        league.setLastSyncedAt(OffsetDateTime.now(clock));
        leagueRepository.save(league);

        for (SleeperUser user : users) {
            Manager manager = managerRepository.findById(user.userId()).orElseGet(Manager::new);
            manager.setId(user.userId());
            manager.setLeagueId(league.getId());
            manager.setDisplayName(user.displayName() != null ? user.displayName() : user.userId());
            manager.setTeamName(user.teamName());
            managerRepository.save(manager);
        }

        Set<String> referencedPlayerIds = sleeperRosters.stream()
                .filter(r -> r.players() != null)
                .flatMap(r -> r.players().stream())
                .collect(Collectors.toSet());
        Map<String, Player> knownPlayers = playerRepository.findAllById(referencedPlayerIds).stream()
                .collect(Collectors.toMap(Player::getId, Function.identity()));

        for (SleeperRoster sleeperRoster : sleeperRosters) {
            Roster roster = rosterRepository
                    .findByLeagueIdAndSleeperRosterId(league.getId(), sleeperRoster.rosterId())
                    .orElseGet(Roster::new);
            roster.setLeagueId(league.getId());
            roster.setSleeperRosterId(sleeperRoster.rosterId());
            roster.setManagerId(sleeperRoster.ownerId()); // null on orphaned rosters, that's fine
            SleeperRoster.Settings settings = sleeperRoster.settings();
            if (settings != null) {
                roster.setWins(settings.wins() != null ? settings.wins() : 0);
                roster.setLosses(settings.losses() != null ? settings.losses() : 0);
                int fpts = settings.fpts() != null ? settings.fpts() : 0;
                int decimals = settings.fptsDecimal() != null ? settings.fptsDecimal() : 0;
                roster.setPointsFor(new BigDecimal(fpts + "." + String.format("%02d", decimals)));
            }
            syncRosterPlayers(roster, sleeperRoster, knownPlayers);
            rosterRepository.save(roster);
        }
    }

    /**
     * Reconciles one roster's player links against Sleeper's current list —
     * diffs instead of clear-and-reinsert, so an unchanged roster is a no-op.
     * @param roster the roster entity being updated (its player set is mutated in place)
     * @param sleeperRoster Sleeper's current player IDs and starters for this roster
     * @param knownPlayers players already found in the local catalog, keyed by ID
     */
    private void syncRosterPlayers(Roster roster, SleeperRoster sleeperRoster, Map<String, Player> knownPlayers) {
        List<String> incomingIds = sleeperRoster.players() != null ? sleeperRoster.players() : List.of();
        Set<String> starterIds = sleeperRoster.starters() != null
                ? new HashSet<>(sleeperRoster.starters())
                : Set.of();

        // diff instead of clear-and-reinsert so an unchanged roster is a no-op
        Map<String, RosterPlayer> existing = new HashMap<>();
        for (RosterPlayer rp : roster.getPlayers()) {
            existing.put(rp.getPlayer().getId(), rp);
        }
        Set<String> incoming = new HashSet<>(incomingIds);
        roster.getPlayers().removeIf(rp -> !incoming.contains(rp.getPlayer().getId()));

        for (String playerId : incomingIds) {
            Player player = knownPlayers.get(playerId);
            if (player == null) {
                // roster references a player id the catalog doesn't have (e.g. IDP)
                log.debug("Skipping unknown player id {} on roster {}", playerId, sleeperRoster.rosterId());
                continue;
            }
            RosterPlayer link = existing.get(playerId);
            if (link != null) {
                link.setStarter(starterIds.contains(playerId));
            } else {
                roster.getPlayers().add(new RosterPlayer(roster, player, starterIds.contains(playerId)));
            }
        }
    }
}
