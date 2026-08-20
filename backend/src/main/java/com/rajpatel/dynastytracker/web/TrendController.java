package com.rajpatel.dynastytracker.web;

import com.rajpatel.dynastytracker.service.TrendService;
import com.rajpatel.dynastytracker.web.dto.TrendSeriesResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoint for the value-over-time chart data. */
@RestController
public class TrendController {

    private final TrendService trendService;

    public TrendController(TrendService trendService) {
        this.trendService = trendService;
    }

    /**
     * @param leagueId the league whose value history to fetch
     * @param from optional inclusive lower date bound (ISO 8601, e.g. 2025-09-01)
     * @param to optional inclusive upper date bound (ISO 8601)
     * @return one value series per roster, for the trend chart
     */
    @GetMapping("/api/leagues/{leagueId}/trends")
    public List<TrendSeriesResponse> getTrends(
            @PathVariable String leagueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return trendService.getTrends(leagueId, from, to);
    }
}
