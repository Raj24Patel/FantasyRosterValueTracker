package com.rajpatel.dynastytracker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A league member (Sleeper user) who owns zero or more rosters. Plain JPA entity — getters/setters only. */
@Entity
@Table(name = "manager")
public class Manager {

    @Id
    private String id; // Sleeper user_id

    private String leagueId;
    private String displayName;
    private String teamName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(String leagueId) {
        this.leagueId = leagueId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
