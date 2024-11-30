package cl.playground.core.util;

import java.util.HashMap;
import java.util.Map;

public class TypeMapper {
    private static final Map<String, String> POSTGRES_TYPE_MAP = new HashMap<>();

    static {
        // Tipos numéricos
        POSTGRES_TYPE_MAP.put("serial", "Long");
        POSTGRES_TYPE_MAP.put("bigserial", "Long");
        POSTGRES_TYPE_MAP.put("int", "Integer");
        POSTGRES_TYPE_MAP.put("integer", "Integer");
        POSTGRES_TYPE_MAP.put("bigint", "Long");
        POSTGRES_TYPE_MAP.put("numeric", "BigDecimal");
        POSTGRES_TYPE_MAP.put("decimal", "BigDecimal");

        // Tipos de texto
        POSTGRES_TYPE_MAP.put("varchar", "String");
        POSTGRES_TYPE_MAP.put("text", "String");
        POSTGRES_TYPE_MAP.put("char", "String");

        // Tipos de fecha/hora
        POSTGRES_TYPE_MAP.put("timestamp", "java.time.LocalDateTime");
        POSTGRES_TYPE_MAP.put("date", "java.time.LocalDate");
        POSTGRES_TYPE_MAP.put("time", "java.time.LocalTime");
    }

    public static String mapPostgresType(String sqlType) {
        String baseType = sqlType.toLowerCase().split("\\s+")[0];
        String mappedType = POSTGRES_TYPE_MAP.get(baseType);

        if (mappedType != null) {
            return mappedType;
        }

        // Si es una referencia a otra entidad, usar el tipo de la entidad
        if (baseType.endsWith("_id")) {
            String referencedEntity = baseType.substring(0, baseType.length() - 3);
            return toPascalCase(referencedEntity);
        }

        return "Object";
    }

    private static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
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
}