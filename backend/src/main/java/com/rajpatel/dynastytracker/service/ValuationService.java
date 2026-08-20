package com.rajpatel.dynastytracker.service;

import com.rajpatel.dynastytracker.config.ValuationProperties;
import com.rajpatel.dynastytracker.domain.Player;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

/**
 * value = base * ageFactor * positionWeight * injuryPenalty
 *
 * Pure function of (player, config) — no I/O, no repositories. Sleeper doesn't
 * publish trade values, so this is a heuristic built on their search_rank field.
 */
@Service
public class ValuationService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final ValuationProperties props;

    public ValuationService(ValuationProperties props) {
        this.props = props;
    }

    public BigDecimal value(Player player) {
        return value(player, props.isSuperflex());
    }

    public BigDecimal value(Player player, boolean superflex) {
        // null search_rank means Sleeper considers the player irrelevant, not rank 0
        if (player.getSearchRank() == null) {
            return ZERO;
        }
        double positionWeight = positionWeight(player.getPosition(), superflex);
        if (positionWeight <= 0) {
            return ZERO;
        }
        double base = props.getBaseScale() * Math.exp(-player.getSearchRank() / props.getRankDecay());
        double ageFactor = ageFactor(player.getPosition(), player.getAge());
        double injuryPenalty = injuryPenalty(player.getInjuryStatus());
        return BigDecimal.valueOf(base * ageFactor * positionWeight * injuryPenalty)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private double positionWeight(String position, boolean superflex) {
        if (position == null) {
            return 0;
        }
        if (superflex && "QB".equals(position)) {
            return props.getSuperflexQbWeight();
        }
        return props.getPositionWeights().getOrDefault(position, 0.0);
    }

    /** Piecewise-linear interpolation over the configured age curve, clamped at both ends. */
    private double ageFactor(String position, Integer age) {
        if (age == null) {
            return 1.0;
        }
        TreeMap<Integer, Double> curve = props.getAgeCurves().get(position);
        if (curve == null || curve.isEmpty()) {
            return 1.0;
        }
        Map.Entry<Integer, Double> floor = curve.floorEntry(age);
        Map.Entry<Integer, Double> ceiling = curve.ceilingEntry(age);
        if (floor == null) {
            return curve.firstEntry().getValue();
        }
        if (ceiling == null) {
            return curve.lastEntry().getValue();
        }
        if (floor.getKey().equals(ceiling.getKey())) {
            return floor.getValue();
        }
        double t = (age - floor.getKey()) / (double) (ceiling.getKey() - floor.getKey());
        return floor.getValue() + t * (ceiling.getValue() - floor.getValue());
    }

    private double injuryPenalty(String injuryStatus) {
        if (injuryStatus == null) {
            return 1.0;
        }
        return props.getInjuryPenalties().getOrDefault(injuryStatus, 1.0);
    }
}
