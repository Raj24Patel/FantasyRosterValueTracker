package com.rajpatel.dynastytracker.service;

import com.rajpatel.dynastytracker.domain.League;
import com.rajpatel.dynastytracker.repository.LeagueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduled trigger that re-syncs every tracked league once a night. */
@Component
public class NightlySyncJob {

    private static final Logger log = LoggerFactory.getLogger(NightlySyncJob.class);

    private final LeagueRepository leagueRepository;
    private final LeagueSyncService leagueSyncService;

    public NightlySyncJob(LeagueRepository leagueRepository, LeagueSyncService leagueSyncService) {
        this.leagueRepository = leagueRepository;
        this.leagueSyncService = leagueSyncService;
    }

    /**
     * Fires on the configured cron schedule (default 4:15am ET). No input;
     * re-syncs every tracked league in turn — one league failing (logged, not
     * thrown) doesn't stop the rest from syncing.
     */
    @Scheduled(cron = "${sync.cron}", zone = "${sync.zone}")
    public void nightlySync() {
        // one league failing shouldn't stop the rest
        for (League league : leagueRepository.findAll()) {
            try {
                leagueSyncService.sync(league.getId());
            } catch (RuntimeException e) {
                log.error("Nightly sync failed for league {}: {}", league.getId(), e.getMessage());
            }
        }
    }
}
