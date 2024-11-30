package cl.playground.core.reader;

import cl.playground.exception.FileReadException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SqlReader {

    public static String readSql(String filePath) throws FileReadException {
        try {
            Path path = Paths.get(filePath);

            // Validaciones básicas
            if (!Files.exists(path)) {
                throw new FileReadException("SQL file not found: " + filePath);
            }

            if (!Files.isRegularFile(path)) {
                throw new FileReadException("Path is not a file: " + filePath);
            }

            if (!filePath.toLowerCase().endsWith(".sql")) {
                throw new FileReadException("File must have .sql extension: " + filePath);
            }

            // Leer el contenido
            String content = Files.readString(path, StandardCharsets.UTF_8);

            // Validar que el contenido no esté vacío
            if (content.trim().isEmpty()) {
                throw new FileReadException("SQL file is empty: " + filePath);
            }

            return content;

        } catch (IOException e) {
            throw new FileReadException("Error reading SQL file: " + filePath, e);
        }
    }
}