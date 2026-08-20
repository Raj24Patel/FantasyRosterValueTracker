package com.rajpatel.dynastytracker.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient sleeperRestClient(RestClient.Builder builder,
                                 @Value("${sleeper.base-url}") String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        // /players/nfl is a ~5MB response, give it room
        factory.setReadTimeout(Duration.ofSeconds(30));
        return builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, "dynasty-tracker/0.1 (github.com/Raj24Patel)")
                .build();
    }
}
