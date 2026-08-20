package com.rajpatel.dynastytracker.web.dto;

import java.math.BigDecimal;

/** One player's computed value within a roster, as returned by the roster detail endpoint. */
public record PlayerValueResponse(
        String playerId,
        String name,
        String position,
        String nflTeam,
        Integer age,
        String injuryStatus,
        boolean starter,
        BigDecimal value) {
}
