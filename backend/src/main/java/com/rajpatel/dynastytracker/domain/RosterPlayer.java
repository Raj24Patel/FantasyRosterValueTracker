package com.rajpatel.dynastytracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "roster_player")
public class RosterPlayer {

    @EmbeddedId
    private RosterPlayerId id = new RosterPlayerId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("rosterId")
    @JoinColumn(name = "roster_id")
    private Roster roster;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playerId")
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(name = "is_starter")
    private boolean starter;

    public RosterPlayer() {
    }

    public RosterPlayer(Roster roster, Player player, boolean starter) {
        this.roster = roster;
        this.player = player;
        this.starter = starter;
    }

    public RosterPlayerId getId() {
        return id;
    }

    public Roster getRoster() {
        return roster;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }
}
