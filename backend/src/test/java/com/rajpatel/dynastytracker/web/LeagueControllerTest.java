package com.rajpatel.dynastytracker.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rajpatel.dynastytracker.repository.LeagueRepository;
import com.rajpatel.dynastytracker.service.LeagueNotFoundException;
import com.rajpatel.dynastytracker.service.LeagueSyncService;
import com.rajpatel.dynastytracker.service.RosterService;
import com.rajpatel.dynastytracker.web.dto.RosterSummaryResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {LeagueController.class, RosterController.class})
class LeagueControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LeagueRepository leagueRepository;
    @MockBean
    LeagueSyncService leagueSyncService;
    @MockBean
    RosterService rosterService;

    @Test
    void getRostersReturnsRankedJson() throws Exception {
        when(rosterService.getPowerRankings(eq("L1"))).thenReturn(List.of(
                summary(1, 11L, "Motor City Misfits", new BigDecimal("4211.50")),
                summary(2, 12L, "Waiver Wire Warriors", new BigDecimal("3877.25"))));

        mockMvc.perform(get("/api/leagues/L1/rosters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].totalValue").value(4211.50))
                .andExpect(jsonPath("$[0].teamName").value("Motor City Misfits"))
                .andExpect(jsonPath("$[1].rank").value(2))
                .andExpect(jsonPath("$[1].totalValue").value(3877.25));
    }

    @Test
    void unknownLeagueReturns404ProblemDetail() throws Exception {
        when(rosterService.getPowerRankings(eq("nope")))
                .thenThrow(new LeagueNotFoundException("nope"));

        mockMvc.perform(get("/api/leagues/nope/rosters"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.title").value("League not found"));
    }

    private static RosterSummaryResponse summary(int rank, Long rosterId, String teamName, BigDecimal total) {
        return new RosterSummaryResponse(rank, rosterId, teamName, "manager", 8, 5,
                new BigDecimal("1543.72"), total,
                new BigDecimal("1200.00"), new BigDecimal("1100.00"),
                new BigDecimal("1400.00"), new BigDecimal("511.50"),
                new BigDecimal("26.4"));
    }
}
