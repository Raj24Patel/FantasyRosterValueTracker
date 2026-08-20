package com.rajpatel.dynastytracker.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "roster")
public class Roster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String leagueId;
    private int sleeperRosterId;
    private String managerId; // null for orphaned rosters
    private int wins;
    private int losses;
    private BigDecimal pointsFor = BigDecimal.ZERO;

    @OneToMany(mappedBy = "roster", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RosterPlayer> players = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(String leagueId) {
        this.leagueId = leagueId;
    }

    public int getSleeperRosterId() {
        return sleeperRosterId;
    }

    public void setSleeperRosterId(int sleeperRosterId) {
        this.sleeperRosterId = sleeperRosterId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public BigDecimal getPointsFor() {
        return pointsFor;
    }

    public void setPointsFor(BigDecimal pointsFor) {
        this.pointsFor = pointsFor;
    }

    public Set<RosterPlayer> getPlayers() {
        return players;
    }
}
