package com.rajpatel.dynastytracker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** One day's computed value for a roster, total and by position — a point on the trend chart. */
@Entity
@Table(name = "value_snapshot")
public class ValueSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roster_id")
    private Roster roster;

    private LocalDate capturedOn;
    private BigDecimal totalValue;
    private BigDecimal qbValue;
    private BigDecimal rbValue;
    private BigDecimal wrValue;
    private BigDecimal teValue;
    private BigDecimal avgAge;

    public Long getId() {
        return id;
    }

    public Roster getRoster() {
        return roster;
    }

    public void setRoster(Roster roster) {
        this.roster = roster;
    }

    public LocalDate getCapturedOn() {
        return capturedOn;
    }

    public void setCapturedOn(LocalDate capturedOn) {
        this.capturedOn = capturedOn;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getQbValue() {
        return qbValue;
    }

    public void setQbValue(BigDecimal qbValue) {
        this.qbValue = qbValue;
    }

    public BigDecimal getRbValue() {
        return rbValue;
    }

    public void setRbValue(BigDecimal rbValue) {
        this.rbValue = rbValue;
    }

    public BigDecimal getWrValue() {
        return wrValue;
    }

    public void setWrValue(BigDecimal wrValue) {
        this.wrValue = wrValue;
    }

    public BigDecimal getTeValue() {
        return teValue;
    }

    public void setTeValue(BigDecimal teValue) {
        this.teValue = teValue;
    }

    public BigDecimal getAvgAge() {
        return avgAge;
    }

    public void setAvgAge(BigDecimal avgAge) {
        this.avgAge = avgAge;
    }
}
