package com.rajpatel.dynastytracker.service;

import com.rajpatel.dynastytracker.domain.League;
import com.rajpatel.dynastytracker.domain.Manager;
import com.rajpatel.dynastytracker.domain.Roster;
import com.rajpatel.dynastytracker.domain.RosterPlayer;
import com.rajpatel.dynastytracker.domain.ValueSnapshot;
import com.rajpatel.dynastytracker.repository.LeagueRepository;
import com.rajpatel.dynastytracker.repository.ManagerRepository;
import com.rajpatel.dynastytracker.repository.RosterRepository;
import com.rajpatel.dynastytracker.repository.ValueSnapshotRepository;
import com.rajpatel.dynastytracker.web.dto.PlayerValueResponse;
import com.rajpatel.dynastytracker.web.dto.RosterDetailResponse;
import com.rajpatel.dynastytracker.web.dto.RosterSummaryResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RosterService {

    private final LeagueRepository leagueRepository;
    private final ManagerRepository managerRepository;
    private final RosterRepository rosterRepository;
    private final ValueSnapshotRepository snapshotRepository;
    private final ValuationService valuationService;

    public RosterService(LeagueRepository leagueRepository,
                         ManagerRepository managerRepository,
                         RosterRepository rosterRepository,
                         ValueSnapshotRepository snapshotRepository,
                         ValuationService valuationService) {
        this.leagueRepository = leagueRepository;
        this.managerRepository = managerRepository;
        this.rosterRepository = rosterRepository;
        this.snapshotRepository = snapshotRepository;
        this.valuationService = valuationService;
    }

    /** Power rankings: rosters ordered by their latest snapshot's total value. */
    @Transactional(readOnly = true)
    public List<RosterSummaryResponse> getPowerRankings(String leagueId) {
        leagueRepository.findById(leagueId).orElseThrow(() -> new LeagueNotFoundException(leagueId));

        Map<String, Manager> managers = managerRepository.findByLeagueId(leagueId).stream()
                .collect(Collectors.toMap(Manager::getId, Function.identity()));

        record Row(Roster roster, ValueSnapshot snapshot) {
        }
        List<Row> rows = new ArrayList<>();
        for (Roster roster : rosterRepository.findByLeagueId(leagueId)) {
            ValueSnapshot latest = snapshotRepository
                    .findTopByRosterIdOrderByCapturedOnDesc(roster.getId())
                    .orElse(null);
            rows.add(new Row(roster, latest));
        }
        rows.sort(Comparator.comparing(
                (Row r) -> r.snapshot() != null ? r.snapshot().getTotalValue() : BigDecimal.ZERO)
                .reversed());

        List<RosterSummaryResponse> rankings = new ArrayList<>();
        int rank = 1;
        for (Row row : rows) {
            Roster roster = row.roster();
            ValueSnapshot snap = row.snapshot();
            Manager manager = roster.getManagerId() != null ? managers.get(roster.getManagerId()) : null;
            rankings.add(new RosterSummaryResponse(
                    rank++,
                    roster.getId(),
                    teamName(manager, roster),
                    manager != null ? manager.getDisplayName() : null,
                    roster.getWins(),
                    roster.getLosses(),
                    roster.getPointsFor(),
                    snap != null ? snap.getTotalValue() : BigDecimal.ZERO,
                    snap != null ? snap.getQbValue() : BigDecimal.ZERO,
                    snap != null ? snap.getRbValue() : BigDecimal.ZERO,
                    snap != null ? snap.getWrValue() : BigDecimal.ZERO,
                    snap != null ? snap.getTeValue() : BigDecimal.ZERO,
                    snap != null ? snap.getAvgAge() : null));
        }
        return rankings;
    }

    /** Roster detail with per-player values, computed live against the current catalog. */
    @Transactional(readOnly = true)
    public RosterDetailResponse getRosterDetail(Long rosterId) {
        Roster roster = rosterRepository.findById(rosterId)
                .orElseThrow(() -> new RosterNotFoundException(rosterId));
        League league = leagueRepository.findById(roster.getLeagueId())
                .orElseThrow(() -> new LeagueNotFoundException(roster.getLeagueId()));
        Manager manager = roster.getManagerId() != null
                ? managerRepository.findById(roster.getManagerId()).orElse(null)
                : null;

        BigDecimal total = BigDecimal.ZERO;
        List<PlayerValueResponse> players = new ArrayList<>();
        for (RosterPlayer link : roster.getPlayers()) {
            BigDecimal value = valuationService.value(link.getPlayer(), league.isSuperflex());
            total = total.add(value);
            players.add(new PlayerValueResponse(
                    link.getPlayer().getId(),
                    link.getPlayer().getFullName(),
                    link.getPlayer().getPosition(),
                    link.getPlayer().getNflTeam(),
                    link.getPlayer().getAge(),
                    link.getPlayer().getInjuryStatus(),
                    link.isStarter(),
                    value));
        }
        players.sort(Comparator.comparing(PlayerValueResponse::value).reversed());

        return new RosterDetailResponse(
                roster.getId(),
                league.getId(),
                league.getName(),
                teamName(manager, roster),
                manager != null ? manager.getDisplayName() : null,
                roster.getWins(),
                roster.getLosses(),
                roster.getPointsFor(),
                total,
                players);
    }

    static String teamName(Manager manager, Roster roster) {
        if (manager != null && manager.getTeamName() != null) {
            return manager.getTeamName();
        }
        if (manager != null) {
            return manager.getDisplayName();
        }
        return "Roster " + roster.getSleeperRosterId();
    }
}
