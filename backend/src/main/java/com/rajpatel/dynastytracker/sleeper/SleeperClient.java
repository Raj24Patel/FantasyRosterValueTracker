package com.rajpatel.dynastytracker.sleeper;

import com.rajpatel.dynastytracker.sleeper.dto.SleeperLeague;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperPlayer;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperRoster;
import com.rajpatel.dynastytracker.sleeper.dto.SleeperUser;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * The only class in the app that knows Sleeper exists.
 * Retries 5xx and network failures twice with backoff; 4xx is never retried.
 */
@Component
public class SleeperClient {

    private static final Logger log = LoggerFactory.getLogger(SleeperClient.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 500;

    private final RestClient restClient;

    public SleeperClient(RestClient sleeperRestClient) {
        this.restClient = sleeperRestClient;
    }

    public SleeperLeague getLeague(String leagueId) {
        return withRetry("GET /league/" + leagueId, () ->
                restClient.get().uri("/league/{id}", leagueId).retrieve().body(SleeperLeague.class));
    }

    public List<SleeperUser> getUsers(String leagueId) {
        return withRetry("GET /league/" + leagueId + "/users", () ->
                restClient.get().uri("/league/{id}/users", leagueId).retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        }));
    }

    public List<SleeperRoster> getRosters(String leagueId) {
        return withRetry("GET /league/" + leagueId + "/rosters", () ->
                restClient.get().uri("/league/{id}/rosters", leagueId).retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        }));
    }

    /** ~5MB response. Callers are responsible for caching this — once a day, max. */
    public Map<String, SleeperPlayer> getAllPlayers() {
        return withRetry("GET /players/nfl", () ->
                restClient.get().uri("/players/nfl").retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        }));
    }

    private <T> T withRetry(String description, Supplier<T> call) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                T body = call.get();
                if (body == null) {
                    // Sleeper returns a literal null body for unknown league ids
                    throw new SleeperApiException("Sleeper returned no data for " + description, 404);
                }
                return body;
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().is4xxClientError()) {
                    throw new SleeperApiException(
                            "Sleeper rejected " + description + " (" + e.getStatusCode().value() + ")",
                            e.getStatusCode().value(), e);
                }
                lastFailure = new SleeperApiException(
                        "Sleeper failed on " + description + " (" + e.getStatusCode().value() + ")",
                        e.getStatusCode().value(), e);
            } catch (ResourceAccessException e) {
                lastFailure = new SleeperApiException(
                        "Sleeper unreachable on " + description, 0, e);
            }
            if (attempt < MAX_ATTEMPTS) {
                long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
                log.warn("{} failed (attempt {}/{}), retrying in {}ms", description, attempt, MAX_ATTEMPTS, backoff);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw lastFailure;
    }
}
