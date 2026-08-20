package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.Manager;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRepository extends JpaRepository<Manager, String> {

    List<Manager> findByLeagueId(String leagueId);
}
