package com.rajpatel.dynastytracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rajpatel.dynastytracker.domain.League;
import com.rajpatel.dynastytracker.domain.Manager;
import com.rajpatel.dynastytracker.domain.Player;
import com.rajpatel.dynastytracker.domain.Roster;
import com.rajpatel.dynastytracker.domain.RosterPlayer;
import com.rajpatel.dynastytracker.domain.SyncLog;
import com.rajpatel.dynastytracker.domain.ValueSnapshot;
import com.rajpatel.dynastytracker.repository.LeagueRepository;
import com.rajpatel.dynastytracker.repository.ManagerRepository;
import com.rajpatel.dynastytracker.repository.PlayerRepository;
import com.rajpatel.dynastytracker.repository.RosterRepository;
import com.rajpatel.dynastytracker.repository.SyncLogRepository;
import com.rajpatel.dynastytracker.repository.ValueSnapshotRepository;
import com.rajpatel.dynastytracker.sleeper.SleeperApiException;
import com.rajpatel.dynastytracker.sleeper.SleeperClient;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperLeague;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperRoster;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperUser;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class LeagueSyncServiceTest {

    private static final String LEAGUE_ID = "L1";
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-10-15T09:15:00Z"), ZoneId.of("America/Detroit"));

    @Mock
    SleeperClient sleeperClient;
    @Mock
    LeagueRepository leagueRepository;
    @Mock
    ManagerRepository managerRepository;
    @Mock
    RosterRepository rosterRepository;
    @Mock
    PlayerRepository playerRepository;
    @Mock
    SyncLogRepository syncLogRepository;
    @Mock
    ValueSnapshotRepository snapshotRepository;
    @Mock
    PlayerCatalogService playerCatalogService;

    // one entry per (rosterId, date) — mirrors the DB's UNIQUE (roster_id, captured_on)
    private final Map<String, ValueSnapshot> snapshotStore = new HashMap<>();

    private LeagueSyncService syncService;
    private League league;
    private Roster rosterOne;
    private Roster rosterTwo;

    @BeforeEach
    void setUp() {
        league = new League();
        league.setId(LEAGUE_ID);
        league.setSuperflex(false);

        Player mahomes = ValuationServiceTest.player("QB", 30, 15, null);
        mahomes.setId("4046");
        Player gibbs = ValuationServiceTest.player("RB", 24, 8, null);
        gibbs.setId("9509");

        rosterOne = roster(1L, 1, "u1", mahomes);
        rosterTwo = roster(2L, 2, null, gibbs);

        lenient().when(sleeperClient.getLeague(LEAGUE_ID)).thenReturn(
                new SleeperLeague(LEAGUE_ID, "Test League", "2025", 2, List.of("QB", "RB", "FLEX")));
        lenient().when(sleeperClient.getUsers(LEAGUE_ID)).thenReturn(
                List.of(new SleeperUser("u1", "raj", Map.of("team_name", "Motor City Misfits"))));
        lenient().when(sleeperClient.getRosters(LEAGUE_ID)).thenReturn(List.of(
                new SleeperRoster(1, "u1", List.of("4046"), List.of("4046"),
                        new SleeperRoster.Settings(8, 5, 1543, 72)),
                new SleeperRoster(2, null, List.of("9509"), List.of(),
                        new SleeperRoster.Settings(5, 8, 1301, 10))));

        lenient().when(leagueRepository.findById(LEAGUE_ID)).thenReturn(Optional.of(league));
        lenient().when(leagueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(managerRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(managerRepository.save(any(Manager.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(playerRepository.findAllById(any())).thenReturn(List.of(mahomes, gibbs));
        lenient().when(rosterRepository.findByLeagueIdAndSleeperRosterId(LEAGUE_ID, 1))
                .thenReturn(Optional.of(rosterOne));
        lenient().when(rosterRepository.findByLeagueIdAndSleeperRosterId(LEAGUE_ID, 2))
                .thenReturn(Optional.of(rosterTwo));
        lenient().when(rosterRepository.save(any(Roster.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(rosterRepository.findByLeagueId(LEAGUE_ID))
                .thenReturn(List.of(rosterOne, rosterTwo));
        when(syncLogRepository.save(any(SyncLog.class))).thenAnswer(inv -> inv.getArgument(0));

        lenient().when(snapshotRepository.findByRosterIdAndCapturedOn(anyLong(), any()))
                .thenAnswer(inv -> Optional.ofNullable(
                        snapshotStore.get(inv.getArgument(0) + "|" + inv.getArgument(1))));
        lenient().when(snapshotRepository.save(any(ValueSnapshot.class))).thenAnswer(inv -> {
            ValueSnapshot s = inv.getArgument(0);
            snapshotStore.put(s.getRoster().getId() + "|" + s.getCapturedOn(), s);
            return s;
        });

        SnapshotService snapshotService = new SnapshotService(
                leagueRepository, rosterRepository, snapshotRepository,
                new ValuationService(ValuationServiceTest.defaultProps()));
        syncService = new LeagueSyncService(
                sleeperClient, leagueRepository, managerRepository, rosterRepository,
                playerRepository, syncLogRepository, playerCatalogService, snapshotService,
                noopTransactionTemplate(), FIXED_CLOCK);
    }

    @Test
    void syncPersistsRostersAndWritesOneSnapshotPerDay() {
        syncService.sync(LEAGUE_ID);
        syncService.sync(LEAGUE_ID); // same simulated day — must not create duplicates

        assertThat(snapshotStore).hasSize(2); // exactly one snapshot per roster
        assertThat(rosterOne.getPlayers()).hasSize(1);
        assertThat(rosterOne.getWins()).isEqualTo(8);
        ValueSnapshot snapshot = snapshotStore.values().iterator().next();
        assertThat(snapshot.getTotalValue()).isPositive();
    }

    @Test
    void failedSleeperCallLeavesExistingRostersIntact() {
        when(sleeperClient.getRosters(LEAGUE_ID))
                .thenThrow(new SleeperApiException("Sleeper failed on GET /rosters (502)", 502));

        assertThatThrownBy(() -> syncService.sync(LEAGUE_ID))
                .isInstanceOf(SleeperApiException.class);

        verify(rosterRepository, never()).deleteAll();
        verify(rosterRepository, never()).delete(any());
        verify(rosterRepository, never()).save(any());
        assertThat(snapshotStore).isEmpty();

        ArgumentCaptor<SyncLog> logCaptor = ArgumentCaptor.forClass(SyncLog.class);
        verify(syncLogRepository, org.mockito.Mockito.atLeast(2)).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo(SyncLog.Status.FAILED);
        assertThat(logCaptor.getValue().getMessage()).contains("502");
    }

    private static Roster roster(Long id, int sleeperRosterId, String managerId, Player player) {
        Roster r = new Roster();
        r.setId(id);
        r.setLeagueId(LEAGUE_ID);
        r.setSleeperRosterId(sleeperRosterId);
        r.setManagerId(managerId);
        r.getPlayers().add(new RosterPlayer(r, player, true));
        return r;
    }

    private static TransactionTemplate noopTransactionTemplate() {
        return new TransactionTemplate(new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {
            }

            @Override
            public void rollback(TransactionStatus status) {
            }
        });
    }
}
