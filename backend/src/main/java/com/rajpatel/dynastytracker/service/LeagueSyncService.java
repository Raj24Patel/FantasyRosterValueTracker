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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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

    public LeagueSyncService(SleeperClient sleeperClient,
                             LeagueRepository leagueRepository,
                             ManagerRepository managerRepository,
                             RosterRepository rosterRepository,
                             PlayerRepository playerRepository,
                             SyncLogRepository syncLogRepository,
                             PlayerCatalogService playerCatalogService,
                             SnapshotService snapshotService,
                             TransactionTemplate transactionTemplate,
                             Clock clock) {
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
    }

    public League addLeague(String sleeperLeagueId) {
        sync(sleeperLeagueId);
        return leagueRepository.findById(sleeperLeagueId)
                .orElseThrow(() -> new LeagueNotFoundException(sleeperLeagueId));
    }

    @Async
    public void syncAsync(String leagueId) {
        try {
            sync(leagueId);
        } catch (RuntimeException e) {
            log.error("Async sync of league {} failed: {}", leagueId, e.getMessage());
        }
    }

    /**
     * Everything is fetched into memory before anything is written, and the write
     * happens in one transaction. A failed sync must never delete existing data —
     * if Sleeper 500s halfway, yesterday's rosters are still there.
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
