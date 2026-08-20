package com.rajpatel.dynastytracker.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One day's snapshot value — a single point on the trend chart. */
public record TrendPointResponse(LocalDate date, BigDecimal totalValue) {
}
