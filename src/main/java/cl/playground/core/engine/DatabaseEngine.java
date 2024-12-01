package cl.playground.core.engine;

import cl.playground.core.model.ClassDefinition;
import java.util.Map;

public interface DatabaseEngine {
    Map<String, ClassDefinition> mapSqlToClassDefinitions(String sqlContent, String basePackage);
    String mapDataType(String sqlType, String columnName, boolean isForeignKey);
}