package cl.playground.core.strategy;

import cl.playground.config.model.SqliftConfig;

import java.util.ArrayList;
import java.util.List;

public class StrategyFactory {

    public static List<EntityStrategy> createStrategies(boolean useLombok, SqliftConfig.PersistenceConfig persistenceConfig) {
        List<EntityStrategy> strategies = new ArrayList<>();

        // Agregar estrategia para Lombok si está habilitado
        if (useLombok) {
            strategies.add(new LombokStrategy());
        }

        // Validar y agregar estrategia de persistencia según el modo
        if (persistenceConfig != null) {
            String mode = persistenceConfig.getMode();
            if (mode == null || mode.isBlank()) {
                throw new IllegalArgumentException("Persistence mode is missing or invalid.");
            }

            switch (mode.toLowerCase()) {
                case "jakarta":
                    strategies.add(new JpaStrategy("jakarta"));
                    break;
                case "hibernate":
                    strategies.add(new JpaStrategy("hibernate"));
                    break;
                case "none":
                    // No se agrega ninguna estrategia relacionada con JPA
                    System.out.println("No persistence annotations will be added (mode: 'none').");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported persistence mode: " + mode);
            }
        }

        return strategies;
    }
}
