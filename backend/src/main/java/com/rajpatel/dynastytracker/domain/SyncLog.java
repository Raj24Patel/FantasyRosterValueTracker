package com.rajpatel.dynastytracker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sync_log")
public class SyncLog {

    public enum Status {
        RUNNING, SUCCESS, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String leagueId;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String message;

    public SyncLog() {
    }

    public SyncLog(String leagueId, OffsetDateTime startedAt) {
        this.leagueId = leagueId;
        this.startedAt = startedAt;
        this.status = Status.RUNNING;
    }

    public Long getId() {
        return id;
    }

    public String getLeagueId() {
        return leagueId;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
