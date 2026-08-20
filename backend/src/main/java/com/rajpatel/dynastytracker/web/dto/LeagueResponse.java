package com.rajpatel.dynastytracker.web.dto;

import com.rajpatel.dynastytracker.domain.League;
import java.time.OffsetDateTime;

/** League header info returned by the leagues endpoints. */
public record LeagueResponse(
        String id,
        String name,
        String season,
        int totalRosters,
        boolean superflex,
        OffsetDateTime lastSyncedAt) {

    /**
     * @param league the entity to convert
     * @return an immutable response DTO mirroring the entity's fields
     */
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
