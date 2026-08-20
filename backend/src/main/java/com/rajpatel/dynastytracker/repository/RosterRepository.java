package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.Roster;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** CRUD access to rosters, keyed by the app's own generated ID (not Sleeper's roster_id). */
public interface RosterRepository extends JpaRepository<Roster, Long> {

    /**
     * @param leagueId the league to look up
     * @return every roster in that league
     */
    List<Roster> findByLeagueId(String leagueId);

    /**
     * Used during sync to find the existing row for a Sleeper roster so it
     * can be updated in place instead of duplicated.
     * @param leagueId the league the roster belongs to
     * @param sleeperRosterId Sleeper's own roster_id (unique within the league, not globally)
     * @return the matching roster, if one has been synced before
     */
    Optional<Roster> findByLeagueIdAndSleeperRosterId(String leagueId, int sleeperRosterId);
}
