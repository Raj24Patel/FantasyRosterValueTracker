package com.rajpatel.dynastytracker.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on {@code @Scheduled} (nightly sync) and {@code @Async} (background re-sync). */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {

    /**
     * Shared system clock, injected wherever "now" is needed instead of calling
     * {@code OffsetDateTime.now()} directly — lets tests substitute a fixed clock.
     * @return the system default-zone clock
     */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
