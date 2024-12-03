package cl.playground.core.strategy;

import cl.playground.config.model.SqliftConfig;

import java.util.ArrayList;
import java.util.List;

public class StrategyFactory {

    public static List<EntityStrategy> createStrategies(boolean useLombok, SqliftConfig.JpaConfig jpaConfig) {
        List<EntityStrategy> strategies = new ArrayList<>();

        if (useLombok) {
            strategies.add(new LombokStrategy());
        }

        if (jpaConfig != null && jpaConfig.isEnabled()) {
            String type = jpaConfig.getType();
            if (type == null || (!type.equalsIgnoreCase("jakarta") && !type.equalsIgnoreCase("javax"))) {
                type = "javax"; // Valor por defecto
            }
            strategies.add(new JpaStrategy(type));
        }

        return strategies;
    }


}
