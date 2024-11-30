package cl.playground.core.engine;

import cl.playground.core.model.ColumnDefinition;
import cl.playground.core.model.TableDefinition;

import java.util.List;
import java.util.Map;

public interface DatabaseEngine {
    List<TableDefinition> parseTables(String sqlContent);
    Map<String, ColumnDefinition> parseColumns(String tableContent);
    String mapDataType(String sqlType);
}
