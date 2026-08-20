package com.rajpatel.dynastytracker.service;

import com.rajpatel.dynastytracker.repository.PlayerRepository;
import com.rajpatel.dynastytracker.sleeper.SleeperClient;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperPlayer;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the daily /players/nfl refresh. The dump is ~5MB and Sleeper's docs
 * ask for at most one call per day, so it is cached in the player table
 * with a freshness guard and never fetched on a request path.
 */
@Service
public class PlayerCatalogService {

    private static final Logger log = LoggerFactory.getLogger(PlayerCatalogService.class);
    private static final Set<String> FANTASY_POSITIONS = Set.of("QB", "RB", "WR", "TE", "K", "DEF");

    private static final String UPSERT_SQL = """
            INSERT INTO player (id, full_name, position, nfl_team, age, years_exp, injury_status, search_rank, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                full_name = EXCLUDED.full_name,
                position = EXCLUDED.position,
                nfl_team = EXCLUDED.nfl_team,
                age = EXCLUDED.age,
                years_exp = EXCLUDED.years_exp,
                injury_status = EXCLUDED.injury_status,
                search_rank = EXCLUDED.search_rank,
                updated_at = EXCLUDED.updated_at
            """;

    private final SleeperClient sleeperClient;
    private final PlayerRepository playerRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private final int maxAgeHours;

    public PlayerCatalogService(SleeperClient sleeperClient,
                                PlayerRepository playerRepository,
                                JdbcTemplate jdbcTemplate,
                                Clock clock,
                                @Value("${sync.player-catalog-max-age-hours:20}") int maxAgeHours) {
        this.sleeperClient = sleeperClient;
        this.playerRepository = playerRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
        this.maxAgeHours = maxAgeHours;
    }

    /**
     * Refreshes the cached player catalog only if it's older than the
     * configured freshness window. No input; called before every league sync.
     * @return true if a refresh actually happened, false if the cache was still fresh
     */
    public boolean refreshIfStale() {
        Optional<OffsetDateTime> newest = playerRepository.findNewestUpdatedAt();
        OffsetDateTime cutoff = OffsetDateTime.now(clock).minusHours(maxAgeHours);
        if (newest.isPresent() && newest.get().isAfter(cutoff)) {
            log.debug("Player catalog is fresh (updated {}), skipping refresh", newest.get());
            return false;
        }
        refresh();
        return true;
    }

    /**
     * Unconditionally fetches Sleeper's full player dump and upserts every
     * fantasy-relevant player (QB/RB/WR/TE/K/DEF) into the player table.
     * No input/output; writes directly to the database.
     */
    @Transactional
    public void refresh() {
        List<SleeperPlayer> relevant = sleeperClient.getAllPlayers().values().stream()
                .filter(p -> p.playerId() != null)
                .filter(p -> p.position() != null && FANTASY_POSITIONS.contains(p.position()))
                .toList();

        Timestamp now = Timestamp.from(OffsetDateTime.now(clock).toInstant());
        jdbcTemplate.batchUpdate(UPSERT_SQL, relevant, 500, (ps, p) -> {
            ps.setString(1, p.playerId());
            ps.setString(2, p.displayName());
            ps.setString(3, p.position());
            ps.setString(4, p.team());
            if (p.age() != null) {
                ps.setInt(5, p.age());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if (p.yearsExp() != null) {
                ps.setInt(6, p.yearsExp());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, p.injuryStatus());
            if (p.searchRank() != null) {
                ps.setInt(8, p.searchRank());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setTimestamp(9, now);
        });
        log.info("Player catalog refreshed: {} fantasy-relevant players upserted", relevant.size());
    }
}
