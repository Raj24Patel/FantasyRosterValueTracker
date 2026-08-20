package com.rajpatel.dynastytracker.web.dto;

import java.math.BigDecimal;

/** One row of the power rankings table. */
public record RosterSummaryResponse(
        int rank,
        Long rosterId,
        String teamName,
        String managerName,
        int wins,
        int losses,
        BigDecimal pointsFor,
        BigDecimal totalValue,
        BigDecimal qbValue,
        BigDecimal rbValue,
        BigDecimal wrValue,
        BigDecimal teValue,
        BigDecimal avgAge) {
}
