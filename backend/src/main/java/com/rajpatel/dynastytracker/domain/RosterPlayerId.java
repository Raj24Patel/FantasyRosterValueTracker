package com.rajpatel.dynastytracker.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Composite primary key (rosterId, playerId) for {@link RosterPlayer}. */
@Embeddable
public class RosterPlayerId implements Serializable {

    private Long rosterId;
    private String playerId;

    public RosterPlayerId() {
    }

    public RosterPlayerId(Long rosterId, String playerId) {
        this.rosterId = rosterId;
        this.playerId = playerId;
    }

    public Long getRosterId() {
        return rosterId;
    }

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RosterPlayerId that)) {
            return false;
        }
        return Objects.equals(rosterId, that.rosterId) && Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rosterId, playerId);
    }
}
