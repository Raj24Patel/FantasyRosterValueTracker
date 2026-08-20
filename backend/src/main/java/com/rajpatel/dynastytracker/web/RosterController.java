package com.rajpatel.dynastytracker.web;

import com.rajpatel.dynastytracker.service.RosterService;
import com.rajpatel.dynastytracker.web.dto.RosterDetailResponse;
import com.rajpatel.dynastytracker.web.dto.RosterSummaryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for power rankings and roster detail. */
@RestController
public class RosterController {

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    /**
     * @param leagueId the league to rank
     * @return every roster in the league, ordered by current total value (rank 1 first)
     */
    @GetMapping("/api/leagues/{leagueId}/rosters")
    public List<RosterSummaryResponse> getPowerRankings(@PathVariable String leagueId) {
        return rosterService.getPowerRankings(leagueId);
    }

    /**
     * @param rosterId the roster to look up
     * @return roster detail with a per-player value breakdown
     */
    @GetMapping("/api/rosters/{rosterId}")
    public RosterDetailResponse getRoster(@PathVariable Long rosterId) {
        return rosterService.getRosterDetail(rosterId);
    }
}
