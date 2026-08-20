package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.League;
import org.springframework.data.jpa.repository.JpaRepository;

/** CRUD access to tracked leagues; keyed by Sleeper league ID. No custom queries needed. */
public interface LeagueRepository extends JpaRepository<League, String> {
}
