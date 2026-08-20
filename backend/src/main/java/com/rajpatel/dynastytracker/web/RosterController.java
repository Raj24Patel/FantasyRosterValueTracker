package com.rajpatel.dynastytracker.web;

import com.rajpatel.dynastytracker.service.RosterService;
import com.rajpatel.dynastytracker.web.dto.RosterDetailResponse;
import com.rajpatel.dynastytracker.web.dto.RosterSummaryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RosterController {

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    @GetMapping("/api/leagues/{leagueId}/rosters")
    public List<RosterSummaryResponse> getPowerRankings(@PathVariable String leagueId) {
        return rosterService.getPowerRankings(leagueId);
    }

    @GetMapping("/api/rosters/{rosterId}")
    public RosterDetailResponse getRoster(@PathVariable Long rosterId) {
        return rosterService.getRosterDetail(rosterId);
    }
}
