package com.rajpatel.dynastytracker.service;

import com.rajpatel.dynastytracker.domain.Manager;
import com.rajpatel.dynastytracker.domain.Roster;
import com.rajpatel.dynastytracker.domain.ValueSnapshot;
import com.rajpatel.dynastytracker.repository.LeagueRepository;
import com.rajpatel.dynastytracker.repository.ManagerRepository;
import com.rajpatel.dynastytracker.repository.RosterRepository;
import com.rajpatel.dynastytracker.repository.ValueSnapshotRepository;
import com.rajpatel.dynastytracker.web.dto.TrendPointResponse;
import com.rajpatel.dynastytracker.web.dto.TrendSeriesResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Builds the value-over-time series (one line per team) for the trend chart. */
@Service
public class TrendService {

    private final LeagueRepository leagueRepository;
    private final ManagerRepository managerRepository;
    private final RosterRepository rosterRepository;
    private final ValueSnapshotRepository snapshotRepository;
    private final Clock clock;

    public TrendService(LeagueRepository leagueRepository,
                        ManagerRepository managerRepository,
                        RosterRepository rosterRepository,
                        ValueSnapshotRepository snapshotRepository,
                        Clock clock) {
        this.leagueRepository = leagueRepository;
        this.managerRepository = managerRepository;
        this.rosterRepository = rosterRepository;
        this.snapshotRepository = snapshotRepository;
        this.clock = clock;
    }

    /**
     * @param leagueId the league whose value history to fetch
     * @param from inclusive lower date bound; defaults to 365 days before `to` if null
     * @param to inclusive upper date bound; defaults to today if null
     * @return one series per roster (even rosters with zero snapshots in range get an empty series)
     * @throws LeagueNotFoundException if the league isn't tracked
     */
    @Transactional(readOnly = true)
    public List<TrendSeriesResponse> getTrends(String leagueId, LocalDate from, LocalDate to) {
        leagueRepository.findById(leagueId).orElseThrow(() -> new LeagueNotFoundException(leagueId));

        LocalDate effectiveTo = to != null ? to : LocalDate.now(clock);
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(365);

        Map<Long, Roster> rosters = rosterRepository.findByLeagueId(leagueId).stream()
                .collect(Collectors.toMap(Roster::getId, Function.identity()));
        Map<String, Manager> managers = managerRepository.findByLeagueId(leagueId).stream()
                .collect(Collectors.toMap(Manager::getId, Function.identity()));

        Map<Long, List<TrendPointResponse>> pointsByRoster = new LinkedHashMap<>();
        rosters.keySet().stream().sorted().forEach(id -> pointsByRoster.put(id, new ArrayList<>()));
        for (ValueSnapshot snapshot : snapshotRepository.findForLeagueInRange(leagueId, effectiveFrom, effectiveTo)) {
            Long rosterId = snapshot.getRoster().getId();
            pointsByRoster.computeIfAbsent(rosterId, k -> new ArrayList<>())
                    .add(new TrendPointResponse(snapshot.getCapturedOn(), snapshot.getTotalValue()));
        }

        List<TrendSeriesResponse> series = new ArrayList<>();
        for (Map.Entry<Long, List<TrendPointResponse>> entry : pointsByRoster.entrySet()) {
            Roster roster = rosters.get(entry.getKey());
            if (roster == null) {
                continue;
            }
            Manager manager = roster.getManagerId() != null ? managers.get(roster.getManagerId()) : null;
            series.add(new TrendSeriesResponse(roster.getId(), RosterService.teamName(manager, roster), entry.getValue()));
        }
        return series;
    }
}
