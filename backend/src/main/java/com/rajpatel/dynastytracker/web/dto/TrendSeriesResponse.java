package com.rajpatel.dynastytracker.web.dto;

import java.util.List;

public record TrendSeriesResponse(Long rosterId, String teamName, List<TrendPointResponse> points) {
}
