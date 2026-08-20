package com.rajpatel.dynastytracker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "league")
public class League {

    @Id
    private String id; // Sleeper league_id

    private String name;
    private String season;
    private int totalRosters;
    private boolean superflex;
    private OffsetDateTime lastSyncedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public int getTotalRosters() {
        return totalRosters;
    }

    public void setTotalRosters(int totalRosters) {
        this.totalRosters = totalRosters;
    }

    public boolean isSuperflex() {
        return superflex;
    }

    public void setSuperflex(boolean superflex) {
        this.superflex = superflex;
    }

    public OffsetDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(OffsetDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
