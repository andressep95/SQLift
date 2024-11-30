package cl.playground.core.strategy;

import java.util.ArrayList;
import java.util.List;

public class StrategyFactory {

    public static List<EntityStrategy> createStrategies(boolean useLombok, boolean useJpa) {
        List<EntityStrategy> strategies = new ArrayList<>();

        if (useLombok) {
            strategies.add(new LombokStrategy());
        }

        if (useJpa) {
            strategies.add(new JpaStrategy());
        }

        return strategies;
    }

}
