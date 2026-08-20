package com.rajpatel.dynastytracker.sleeper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SleeperRoster(
        @JsonProperty("roster_id") Integer rosterId,
        @JsonProperty("owner_id") String ownerId, // null on orphaned rosters
        List<String> players,   // player ids, but team codes ("DET") for defenses
        List<String> starters,
        Settings settings) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Settings(
            Integer wins,
            Integer losses,
            Integer fpts,
            @JsonProperty("fpts_decimal") Integer fptsDecimal) {
    }
}
