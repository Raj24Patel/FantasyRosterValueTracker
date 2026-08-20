package com.rajpatel.dynastytracker.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rajpatel.dynastytracker.domain.League;
import com.rajpatel.dynastytracker.domain.Roster;
import com.rajpatel.dynastytracker.domain.ValueSnapshot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs against real Postgres via Testcontainers — H2 happily accepts SQL
 * that Postgres rejects, so this exercises the same database we deploy on
 * (including the Flyway migration and the unique constraint).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ValueSnapshotRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    LeagueRepository leagueRepository;
    @Autowired
    RosterRepository rosterRepository;
    @Autowired
    ValueSnapshotRepository snapshotRepository;

    @Test
    void findsSnapshotsInDateRangeOrderedByDate() {
        Roster roster = persistLeagueWithRoster();
        snapshotRepository.save(snapshot(roster, LocalDate.of(2025, 10, 3), "3100.00"));
        snapshotRepository.save(snapshot(roster, LocalDate.of(2025, 10, 1), "3000.00"));
        snapshotRepository.save(snapshot(roster, LocalDate.of(2025, 10, 2), "3050.00"));
        snapshotRepository.save(snapshot(roster, LocalDate.of(2025, 10, 9), "3300.00"));

        List<ValueSnapshot> found = snapshotRepository.findForLeagueInRange(
                "L1", LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 3));

        assertThat(found).hasSize(3);
        assertThat(found).extracting(ValueSnapshot::getCapturedOn).isSorted();
        assertThat(found.get(0).getTotalValue()).isEqualByComparingTo("3000.00");
    }

    @Test
    void duplicateSnapshotForSameDayViolatesUniqueConstraint() {
        Roster roster = persistLeagueWithRoster();
        snapshotRepository.saveAndFlush(snapshot(roster, LocalDate.of(2025, 10, 1), "3000.00"));

        assertThatThrownBy(() ->
                snapshotRepository.saveAndFlush(snapshot(roster, LocalDate.of(2025, 10, 1), "9999.00")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Roster persistLeagueWithRoster() {
        League league = new League();
        league.setId("L1");
        league.setName("Test League");
        league.setSeason("2025");
        league.setTotalRosters(12);
        leagueRepository.save(league);

        Roster roster = new Roster();
        roster.setLeagueId("L1");
        roster.setSleeperRosterId(1);
        return rosterRepository.save(roster);
    }

    private static ValueSnapshot snapshot(Roster roster, LocalDate date, String total) {
        ValueSnapshot s = new ValueSnapshot();
        s.setRoster(roster);
        s.setCapturedOn(date);
        s.setTotalValue(new BigDecimal(total));
        s.setQbValue(BigDecimal.ZERO);
        s.setRbValue(BigDecimal.ZERO);
        s.setWrValue(BigDecimal.ZERO);
        s.setTeValue(BigDecimal.ZERO);
        return s;
    }
}
