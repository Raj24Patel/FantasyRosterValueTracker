package com.rajpatel.dynastytracker.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record RosterDetailResponse(
        Long rosterId,
        String leagueId,
        String leagueName,
        String teamName,
        String managerName,
        int wins,
        int losses,
        BigDecimal pointsFor,
        BigDecimal totalValue,
        List<PlayerValueResponse> players) {
}
