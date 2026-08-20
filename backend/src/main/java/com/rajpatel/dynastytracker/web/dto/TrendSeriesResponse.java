package com.rajpatel.dynastytracker.web.dto;

import java.util.List;

/** One roster's full value history — a single line on the trend chart. */
public record TrendSeriesResponse(Long rosterId, String teamName, List<TrendPointResponse> points) {
}
