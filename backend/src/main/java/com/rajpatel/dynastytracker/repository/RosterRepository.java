package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.Roster;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RosterRepository extends JpaRepository<Roster, Long> {

    List<Roster> findByLeagueId(String leagueId);

    Optional<Roster> findByLeagueIdAndSleeperRosterId(String leagueId, int sleeperRosterId);
}
