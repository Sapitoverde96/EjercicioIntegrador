package com.cesur.valciro.data.config;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;


public class DatabaseInitializer {
    /**
     * Crea las tablas y, si están vacías, carga los datos de ejemplo. Se usa el
     * fichero {@code candidaturas.csv} si existe. Si no existe usa el fichero {@code data.sql}.
     *
     * @param datasource origen de datos desde el que se obtiene la conexion
     * @throws RuntimeException si no se pueden crear las tablas ni cargar los datos
     */
    public static void init(DataSource datasource) {
        runSQL(datasource, "schema.sql");
        if (tablesAreEmpty(datasource)) {
            Path initialCsv = Path.of("candidaturas.csv");
            if (Files.isRegularFile(initialCsv)) {
                CsvImporter.importFile(datasource, initialCsv);
            } else {
                runSQL(datasource, "data.sql");
            }
        }
    }

    /**
     * Comprueba si no hay ofertas guardadas.
     *
     * @param datasource origen de datos desde el que se obtiene la conexión
     * @return {@code true} si no hay ninguna oferta, {@code false} si hay oferta
     */
    public static boolean tablesAreEmpty(DataSource datasource) {
        String sqlStatement = "SELECT COUNT(*) FROM offer";
        try (
                Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rtSet = statement.executeQuery(sqlStatement)
        ) {
            return rtSet.next() && rtSet.getLong(1) == 0;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Ejecuta las instrucciones SQL que contiene un fichero del proyecto.
     *
     * @param datasource origen de datos desde el que se obtiene la conexión
     * @param sqlFile    nombre del fichero SQL
     * @throws IllegalArgumentException si el fichero no termina en {@code .sql}
     * @throws RuntimeException         si no se encuentra el ficherp o falla algo en sql
     */
    private static void runSQL(DataSource datasource, String sqlFile) {
        try (InputStream inStream = DatabaseInitializer.class.getResourceAsStream("/" + sqlFile)) {
            if (inStream == null) {
                throw new FileNotFoundException("File not found: " + sqlFile);
            }
            if (!sqlFile.endsWith(".sql")) {
                throw new IllegalArgumentException("File does not have .sql extension: " + sqlFile);
            }
            String sqlStatements;
            try (BufferedReader bfReader = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8))) {
                sqlStatements = bfReader.lines().collect(Collectors.joining("\n"));
            }
            try (Connection connection = datasource.getConnection(); Statement statement = connection.createStatement()) {
                for (String sqlStatement : sqlStatements.split(";")) {
                    if (!sqlStatement.isBlank()) { statement.execute(sqlStatement); }
                }
            }
        } catch (IOException | SQLException exception) {
            throw new RuntimeException("Error running SQL file \"" + sqlFile + "\": ", exception);
        }
    }
}
