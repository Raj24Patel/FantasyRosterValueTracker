package com.rajpatel.dynastytracker.sleeper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One entry from {@code GET /players/nfl} — a player or a team defense. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SleeperPlayer(
        @JsonProperty("player_id") String playerId,
        @JsonProperty("full_name") String fullName, // null for team defenses
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String position,
        String team,
        Integer age,
        @JsonProperty("years_exp") Integer yearsExp,
        @JsonProperty("injury_status") String injuryStatus,
        @JsonProperty("search_rank") Integer searchRank) {

    /**
     * Defenses come through with only first/last name ("Detroit" / "Lions"),
     * so this fills in a usable name either way.
     * @return the player's full name, or the player ID as a last resort
     */
    public String displayName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return playerId;
    }
}
