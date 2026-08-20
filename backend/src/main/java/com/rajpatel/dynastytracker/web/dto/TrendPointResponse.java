package com.rajpatel.dynastytracker.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrendPointResponse(LocalDate date, BigDecimal totalValue) {
}
