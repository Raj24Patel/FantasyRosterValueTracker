package com.rajpatel.dynastytracker.web.dto;

import java.math.BigDecimal;

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
