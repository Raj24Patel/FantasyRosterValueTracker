package com.rajpatel.dynastytracker.config;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunable knobs for the player valuation formula, bound from the
 * {@code valuation.*} keys in application.yml. Plain config bean —
 * getters/setters only, no logic (that lives in {@code ValuationService}).
 */
@ConfigurationProperties(prefix = "valuation")
public class ValuationProperties {

    private boolean superflex = false;
    private double superflexQbWeight = 1.35;
    private double baseScale = 100;
    private double rankDecay = 250;
    private Map<String, Double> positionWeights = new HashMap<>();
    private Map<String, TreeMap<Integer, Double>> ageCurves = new HashMap<>();
    private Map<String, Double> injuryPenalties = new HashMap<>();

    public boolean isSuperflex() {
        return superflex;
    }

    public void setSuperflex(boolean superflex) {
        this.superflex = superflex;
    }

    public double getSuperflexQbWeight() {
        return superflexQbWeight;
    }

    public void setSuperflexQbWeight(double superflexQbWeight) {
        this.superflexQbWeight = superflexQbWeight;
    }

    public double getBaseScale() {
        return baseScale;
    }

    public void setBaseScale(double baseScale) {
        this.baseScale = baseScale;
    }

    public double getRankDecay() {
        return rankDecay;
    }

    public void setRankDecay(double rankDecay) {
        this.rankDecay = rankDecay;
    }

    public Map<String, Double> getPositionWeights() {
        return positionWeights;
    }

    public void setPositionWeights(Map<String, Double> positionWeights) {
        this.positionWeights = positionWeights;
    }

    public Map<String, TreeMap<Integer, Double>> getAgeCurves() {
        return ageCurves;
    }

    public void setAgeCurves(Map<String, TreeMap<Integer, Double>> ageCurves) {
        this.ageCurves = ageCurves;
    }

    public Map<String, Double> getInjuryPenalties() {
        return injuryPenalties;
    }

    public void setInjuryPenalties(Map<String, Double> injuryPenalties) {
        this.injuryPenalties = injuryPenalties;
    }
}
