package cl.playground.core.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeMapper {
    private static final List<String> IGNORED_SQL_TYPES = Arrays.asList(
            "key", "primary", "foreign", "null", "default", "constraint",
            "references", "check", "unique", "current_timestamp"
    );

    private static final Map<String, String> POSTGRES_TYPE_MAP = new HashMap<>();

    static {
        // Tipos numéricos
        POSTGRES_TYPE_MAP.put("serial", "Integer");
        POSTGRES_TYPE_MAP.put("bigserial", "Long");
        POSTGRES_TYPE_MAP.put("int", "Integer");
        POSTGRES_TYPE_MAP.put("integer", "Integer");
        POSTGRES_TYPE_MAP.put("bigint", "Long");
        POSTGRES_TYPE_MAP.put("numeric", "java.math.BigDecimal");
        POSTGRES_TYPE_MAP.put("decimal", "java.math.BigDecimal");
        POSTGRES_TYPE_MAP.put("real", "Float");
        POSTGRES_TYPE_MAP.put("double precision", "Double");

        // Tipos de texto
        POSTGRES_TYPE_MAP.put("varchar", "String");
        POSTGRES_TYPE_MAP.put("text", "String");
        POSTGRES_TYPE_MAP.put("char", "String");

        // Tipos de fecha/hora
        POSTGRES_TYPE_MAP.put("timestamp", "java.time.LocalDateTime");
        POSTGRES_TYPE_MAP.put("date", "java.time.LocalDate");
        POSTGRES_TYPE_MAP.put("time", "java.time.LocalTime");

        // Tipos booleanos
        POSTGRES_TYPE_MAP.put("boolean", "Boolean");
        POSTGRES_TYPE_MAP.put("bool", "Boolean");
    }

    public static String mapPostgresType(String sqlType, String columnName, boolean isForeignKey) {
        if (isIgnoredSqlType(sqlType)) {
            return null;
        }

        // Si es una foreign key, usar el nombre de la entidad
        if (isForeignKey) {
            String entityName = columnName.replace("_id", "");
            return toPascalCase(entityName);
        }

        // Para tipos normales
        String baseType = sqlType.toLowerCase().split("\\s+")[0];
        String javaType = POSTGRES_TYPE_MAP.get(baseType);

        if (javaType == null) {
            // Verificar si es una referencia a clase (empieza con mayúscula)
            if (Character.isUpperCase(sqlType.charAt(0))) {
                return sqlType;
            }
            System.out.println("Tipo SQL no reconocido: " + sqlType + ". Usando Object por defecto.");
            return "Object";
        }

        return javaType;
    }

    private static boolean isIgnoredSqlType(String sqlType) {
        return IGNORED_SQL_TYPES.contains(sqlType.toLowerCase());
    }

    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Manejar plurales
        if (input.endsWith("es")) {
            input = input.substring(0, input.length() - 2);
        } else if (input.endsWith("s")) {
            input = input.substring(0, input.length() - 1);
        }

        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    // Método para agregar mapeos personalizados si es necesario
    public static void addCustomMapping(String sqlType, String javaType) {
        POSTGRES_TYPE_MAP.put(sqlType.toLowerCase(), javaType);
    }
}