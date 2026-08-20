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

@RestController
public class TrendController {

    private final TrendService trendService;

    public TrendController(TrendService trendService) {
        this.trendService = trendService;
    }

    @GetMapping("/api/leagues/{leagueId}/trends")
    public List<TrendSeriesResponse> getTrends(
            @PathVariable String leagueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return trendService.getTrends(leagueId, from, to);
    }
}
