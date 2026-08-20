package com.rajpatel.dynastytracker.sleeper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response shape of {@code GET /league/{leagueId}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SleeperLeague(
        @JsonProperty("league_id") String leagueId,
        String name,
        String season,
        @JsonProperty("total_rosters") Integer totalRosters,
        @JsonProperty("roster_positions") List<String> rosterPositions) {

    /** @return true if the league's starting lineup includes a SUPER_FLEX slot */
    public boolean isSuperflex() {
        return rosterPositions != null && rosterPositions.contains("SUPER_FLEX");
    }
}
