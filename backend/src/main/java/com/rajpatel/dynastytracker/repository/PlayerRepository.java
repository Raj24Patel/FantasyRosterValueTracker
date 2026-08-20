package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.Player;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerRepository extends JpaRepository<Player, String> {

    @Query("select max(p.updatedAt) from Player p")
    Optional<OffsetDateTime> findNewestUpdatedAt();
}
