package cl.playground.core.engine;

import cl.playground.exception.EngineException;

public class DatabaseEngineFactory {
    public static DatabaseEngine createEngine(String engineType) {
        switch (engineType.toLowerCase()) {
            case "postgresql":
                return new PostgresEngine();
            // Otros casos para futuros motores
            default:
                throw new EngineException("Unsupported database engine: " + engineType);
        }
    }
}
