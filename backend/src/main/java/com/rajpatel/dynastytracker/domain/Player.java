package com.rajpatel.dynastytracker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * One NFL player (or team defense) from Sleeper's player catalog, cached
 * locally so valuation never needs a live Sleeper call. Plain JPA entity —
 * getters/setters only.
 */
@Entity
@Table(name = "player")
public class Player {

    @Id
    private String id; // Sleeper player_id, or a team code like "DET" for defenses

    private String fullName;
    private String position;
    private String nflTeam;
    private Integer age;
    private Integer yearsExp;
    private String injuryStatus;
    private Integer searchRank; // null means Sleeper considers the player irrelevant
    private OffsetDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getNflTeam() {
        return nflTeam;
    }

    public void setNflTeam(String nflTeam) {
        this.nflTeam = nflTeam;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getYearsExp() {
        return yearsExp;
    }

    public void setYearsExp(Integer yearsExp) {
        this.yearsExp = yearsExp;
    }

    public String getInjuryStatus() {
        return injuryStatus;
    }

    public void setInjuryStatus(String injuryStatus) {
        this.injuryStatus = injuryStatus;
    }

    public Integer getSearchRank() {
        return searchRank;
    }

    public void setSearchRank(Integer searchRank) {
        this.searchRank = searchRank;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
