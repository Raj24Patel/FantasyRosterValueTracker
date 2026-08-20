package com.rajpatel.dynastytracker.service;

import com.rajpatel.dynastytracker.domain.League;
import com.rajpatel.dynastytracker.domain.Player;
import com.rajpatel.dynastytracker.domain.Roster;
import com.rajpatel.dynastytracker.domain.RosterPlayer;
import com.rajpatel.dynastytracker.domain.ValueSnapshot;
import com.rajpatel.dynastytracker.repository.LeagueRepository;
import com.rajpatel.dynastytracker.repository.RosterRepository;
import com.rajpatel.dynastytracker.repository.ValueSnapshotRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Computes and stores each roster's daily value snapshot — the data behind the trend chart. */
@Service
public class SnapshotService {

    private final LeagueRepository leagueRepository;
    private final RosterRepository rosterRepository;
    private final ValueSnapshotRepository snapshotRepository;
    private final ValuationService valuationService;

    public SnapshotService(LeagueRepository leagueRepository,
                           RosterRepository rosterRepository,
                           ValueSnapshotRepository snapshotRepository,
                           ValuationService valuationService) {
        this.leagueRepository = leagueRepository;
        this.rosterRepository = rosterRepository;
        this.snapshotRepository = snapshotRepository;
        this.valuationService = valuationService;
    }

    /**
     * Values every roster in a league as of `date` and upserts one snapshot
     * row per roster. Re-running for the same date updates the existing row
     * instead of adding a duplicate point (backed by the UNIQUE
     * (roster_id, captured_on) constraint), so re-syncs are idempotent.
     * @param leagueId the league whose rosters to snapshot
     * @param date the date to record the snapshot under (usually today)
     */
    @Transactional
    public void captureSnapshots(String leagueId, LocalDate date) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException(leagueId));

        for (Roster roster : rosterRepository.findByLeagueId(leagueId)) {
            ValueSnapshot snapshot = snapshotRepository
                    .findByRosterIdAndCapturedOn(roster.getId(), date)
                    .orElseGet(ValueSnapshot::new);
            snapshot.setRoster(roster);
            snapshot.setCapturedOn(date);

            BigDecimal total = BigDecimal.ZERO;
            BigDecimal qb = BigDecimal.ZERO;
            BigDecimal rb = BigDecimal.ZERO;
            BigDecimal wr = BigDecimal.ZERO;
            BigDecimal te = BigDecimal.ZERO;
            List<Integer> ages = new ArrayList<>();

            for (RosterPlayer link : roster.getPlayers()) {
                Player player = link.getPlayer();
                BigDecimal value = valuationService.value(player, league.isSuperflex());
                total = total.add(value);
                switch (player.getPosition() == null ? "" : player.getPosition()) {
                    case "QB" -> qb = qb.add(value);
                    case "RB" -> rb = rb.add(value);
                    case "WR" -> wr = wr.add(value);
                    case "TE" -> te = te.add(value);
                    default -> {
                    }
                }
                if (player.getAge() != null) {
                    ages.add(player.getAge());
                }
            }

            snapshot.setTotalValue(total);
            snapshot.setQbValue(qb);
            snapshot.setRbValue(rb);
            snapshot.setWrValue(wr);
            snapshot.setTeValue(te);
            snapshot.setAvgAge(averageAge(ages));
            snapshotRepository.save(snapshot);
        }
    }

    /**
     * @param ages ages of every player on the roster who has a known age
     * @return the mean age rounded to one decimal, or null if no ages are known
     */
    private BigDecimal averageAge(List<Integer> ages) {
        if (ages.isEmpty()) {
            return null;
        }
        int sum = ages.stream().mapToInt(Integer::intValue).sum();
        return BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(ages.size()), 1, RoundingMode.HALF_UP);
    }
}
