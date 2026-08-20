package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.ValueSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ValueSnapshotRepository extends JpaRepository<ValueSnapshot, Long> {

    Optional<ValueSnapshot> findByRosterIdAndCapturedOn(Long rosterId, LocalDate capturedOn);

    Optional<ValueSnapshot> findTopByRosterIdOrderByCapturedOnDesc(Long rosterId);

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
