package cl.playground.core.strategy;

import cl.playground.config.model.SqliftConfig;

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

        System.out.println("Estrategias creadas: " + strategies);
        return strategies;
    }
}
