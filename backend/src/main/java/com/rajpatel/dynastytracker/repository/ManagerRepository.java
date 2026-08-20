package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.Manager;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** CRUD access to league managers, keyed by Sleeper user ID. */
public interface ManagerRepository extends JpaRepository<Manager, String> {

    /**
     * @param leagueId the league to look up
     * @return every manager belonging to that league
     */
    List<Manager> findByLeagueId(String leagueId);
}
