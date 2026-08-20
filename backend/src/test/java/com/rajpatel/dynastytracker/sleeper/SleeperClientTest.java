package com.rajpatel.dynastytracker.sleeper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.rajpatel.dynastytracker.sleeper.dto.SleeperRoster;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SleeperClientTest {

    private static final String BASE = "https://api.sleeper.app/v1";

    private MockRestServiceServer server;
    private SleeperClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SleeperClient(builder.baseUrl(BASE).build());
    }

    @Test
    void parsesRosterFixtureIncludingDefenseEntries() {
        // saved from a real GET /league/{id}/rosters response
        server.expect(requestTo(BASE + "/league/1180213086679706624/rosters"))
                .andRespond(withSuccess(new ClassPathResource("fixtures/rosters.json"),
                        MediaType.APPLICATION_JSON));

        List<SleeperRoster> rosters = client.getRosters("1180213086679706624");

        assertThat(rosters).hasSize(2);

        SleeperRoster first = rosters.get(0);
        // rosters reference players by id string only — numeric ids AND team codes for defenses
        assertThat(first.players()).contains("1046", "DET");
        assertThat(first.starters()).contains("DET");
        assertThat(first.ownerId()).isEqualTo("783456789012345678");
        assertThat(first.settings().wins()).isEqualTo(8);
        assertThat(first.settings().fpts()).isEqualTo(1543);

        // orphaned roster: owner_id is null and must not blow up
        SleeperRoster second = rosters.get(1);
        assertThat(second.ownerId()).isNull();
        assertThat(second.players()).contains("SF");

        server.verify();
    }
}
