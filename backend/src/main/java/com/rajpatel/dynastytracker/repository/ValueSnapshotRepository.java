package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.ValueSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** CRUD access to daily value snapshots — the data behind the trend chart. */
public interface ValueSnapshotRepository extends JpaRepository<ValueSnapshot, Long> {

    /**
     * Used by SnapshotService to upsert idempotently: load today's row for a
     * roster (if it already exists) instead of inserting a duplicate.
     * @param rosterId the roster to look up
     * @param capturedOn the snapshot date
     * @return the existing snapshot for that roster+date, if any
     */
    Optional<ValueSnapshot> findByRosterIdAndCapturedOn(Long rosterId, LocalDate capturedOn);

    /**
     * @param rosterId the roster to look up
     * @return that roster's most recent snapshot, used for the power rankings table
     */
    Optional<ValueSnapshot> findTopByRosterIdOrderByCapturedOnDesc(Long rosterId);

    /**
     * @param leagueId the league whose history to fetch
     * @param from inclusive lower date bound
     * @param to inclusive upper date bound
     * @return every snapshot for the league in that range, oldest first, for the trend chart
     */
    @Query("""
            select vs from ValueSnapshot vs
            where vs.roster.leagueId = :leagueId
              and vs.capturedOn between :from and :to
            order by vs.capturedOn, vs.roster.id
            """)
    List<ValueSnapshot> findForLeagueInRange(@Param("leagueId") String leagueId,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to);
}
