package cl.playground.util;

import java.util.Map;

public class LogContent {

    public static void logConfiguration(Map<String, Object> context) {
        System.out.println("📝 Configuration loaded:");
        System.out.println("  - Engine: " + context.get("engine"));
        System.out.println("  - Schema: " + context.get("schema"));
        System.out.println("  - Output Package: " + context.get("outputPackage"));
        System.out.println("  - Lombok: " + context.get("useLombok"));
        System.out.println("  - JPA: " + context.get("useJpa"));
    }
    public static void logSqlContent(String sqlContent) {
        System.out.println("\n📄 SQL Content loaded:");
        System.out.println("----------------------------------------");
        System.out.println(sqlContent);
        System.out.println("----------------------------------------");
    }

}
