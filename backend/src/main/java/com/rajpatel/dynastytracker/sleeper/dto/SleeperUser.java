package com.rajpatel.dynastytracker.sleeper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SleeperUser(
        @JsonProperty("user_id") String userId,
        @JsonProperty("display_name") String displayName,
        Map<String, Object> metadata) {

    public String teamName() {
        if (metadata == null) {
            return null;
        }
        Object teamName = metadata.get("team_name");
        return teamName instanceof String s && !s.isBlank() ? s : null;
    }
}
