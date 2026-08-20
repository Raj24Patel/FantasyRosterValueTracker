package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.League;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepository extends JpaRepository<League, String> {
}
