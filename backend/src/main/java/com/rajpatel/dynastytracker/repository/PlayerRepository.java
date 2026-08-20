package com.rajpatel.dynastytracker.repository;

import com.rajpatel.dynastytracker.domain.Player;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** CRUD access to the cached player catalog, keyed by Sleeper player ID (or team code for defenses). */
public interface PlayerRepository extends JpaRepository<Player, String> {

    /**
     * Used by the freshness guard to decide whether the daily /players/nfl
     * refresh is due yet.
     * @return the most recent `updated_at` across all cached players, or empty if the table is empty
     */
    @Query("select max(p.updatedAt) from Player p")
    Optional<OffsetDateTime> findNewestUpdatedAt();
}
