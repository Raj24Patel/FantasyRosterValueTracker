package com.rajpatel.dynastytracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.rajpatel.dynastytracker.config.ValuationProperties;
import com.rajpatel.dynastytracker.domain.Player;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Pure logic, no Spring context. */
class ValuationServiceTest {

    @Test
    void youngerRunningBackScoresHigherThanOlderAtSameRank() {
        ValuationService service = new ValuationService(defaultProps());

        BigDecimal young = service.value(player("RB", 23, 5, null));
        BigDecimal old = service.value(player("RB", 29, 5, null));

        assertThat(young).isGreaterThan(old);
    }

    @Test
    void superflexConfigRaisesQuarterbackValue() {
        ValuationProperties standard = defaultProps();
        standard.setSuperflex(false);
        ValuationProperties superflex = defaultProps();
        superflex.setSuperflex(true);

        Player qb = player("QB", 27, 10, null);
        BigDecimal standardValue = new ValuationService(standard).value(qb);
        BigDecimal superflexValue = new ValuationService(superflex).value(qb);

        assertThat(superflexValue).isGreaterThan(standardValue);
    }

    @ParameterizedTest
    @MethodSource("junkPlayers")
    void nullSearchRankAndUnknownPositionYieldZeroWithoutThrowing(Player junk) {
        ValuationService service = new ValuationService(defaultProps());

        BigDecimal value = assertDoesNotThrow(() -> service.value(junk));

        assertThat(value).isEqualByComparingTo(BigDecimal.ZERO);
    }

    static Stream<Player> junkPlayers() {
        return Stream.of(
                player("RB", 24, null, null),   // null search_rank means irrelevant, not rank 0
                player("P", 28, 40, null),      // punter — no position weight configured
                player(null, 25, 40, null),     // no position at all
                player(null, null, null, null)  // fully empty record
        );
    }

    static Player player(String position, Integer age, Integer searchRank, String injuryStatus) {
        Player p = new Player();
        p.setId("test");
        p.setFullName("Test Player");
        p.setPosition(position);
        p.setAge(age);
        p.setSearchRank(searchRank);
        p.setInjuryStatus(injuryStatus);
        return p;
    }

    static ValuationProperties defaultProps() {
        ValuationProperties props = new ValuationProperties();
        props.setPositionWeights(new HashMap<>(Map.of(
                "QB", 0.85, "RB", 1.00, "WR", 1.05, "TE", 0.90, "K", 0.05, "DEF", 0.05)));
        Map<String, TreeMap<Integer, Double>> curves = new HashMap<>();
        curves.put("RB", new TreeMap<>(Map.of(22, 1.15, 24, 1.20, 30, 0.55, 34, 0.20)));
        curves.put("WR", new TreeMap<>(Map.of(22, 1.10, 26, 1.15, 30, 0.75, 34, 0.40)));
        curves.put("TE", new TreeMap<>(Map.of(22, 1.00, 27, 1.10, 30, 0.85, 34, 0.55)));
        curves.put("QB", new TreeMap<>(Map.of(22, 1.05, 29, 1.10, 30, 1.05, 34, 0.85)));
        props.setAgeCurves(curves);
        props.setInjuryPenalties(new HashMap<>(Map.of(
                "IR", 0.85, "Out", 0.85, "Questionable", 0.95, "Doubtful", 0.95)));
        return props;
    }
}
