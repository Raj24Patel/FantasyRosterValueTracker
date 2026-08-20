package com.rajpatel.dynastytracker.web.dto;

import com.rajpatel.dynastytracker.domain.League;
import java.time.OffsetDateTime;

public record LeagueResponse(
        String id,
        String name,
        String season,
        int totalRosters,
        boolean superflex,
        OffsetDateTime lastSyncedAt) {

    public static LeagueResponse from(League league) {
        return new LeagueResponse(
                league.getId(),
                league.getName(),
                league.getSeason(),
                league.getTotalRosters(),
                league.isSuperflex(),
                league.getLastSyncedAt());
    }
}
