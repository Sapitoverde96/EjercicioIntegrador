package com.cesur.valciro.data.config;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;


public class DatasourceFactory {
    /** Nombre del fichero con los ajustes de la base de datos. */
    private static final String PROPERTIES_FILE = "database.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:32768/database";

    private static DataSource datasource = null;

    private DatasourceFactory() {}

    /**
     * Devuelve el origen de datos de la base de datos. La primera vez lee la
     * configuración y lo crea; en las siguientes llamadas devuelve el mismo.
     *
     * @throws IllegalStateException si no tiene el usuario o la contraseña
     * @throws RuntimeException      si no se puede leer el fichero de configuración
     */
    public static DataSource getDataSource() {
        if (datasource != null) {
            return datasource;
        }

        Properties properties = loadProperties();
        String databaseURL = setting("db.url", "DB_URL", properties, DEFAULT_URL);
        String databaseUsername = setting("db.username", "DB_USER", properties, null);
        String databasePassword = setting("db.password", "DB_PSWD", properties, null);

        if (databaseUsername == null || databaseUsername.isBlank()) {
            throw new IllegalStateException(
                    "Database username not specified. Set DB_USER or db.username in database.properties");
        }
        if (databasePassword == null) {
            throw new IllegalStateException(
                    "Database password not specified. Set DB_PSWD or db.password in database.properties");
        }

        MysqlDataSource mysqlDatasource = new MysqlDataSource();
        mysqlDatasource.setURL(databaseURL);
        mysqlDatasource.setUser(databaseUsername);
        mysqlDatasource.setPassword(databasePassword);
        datasource = mysqlDatasource;
        return datasource;
    }

    /**
     * Lee los ajustes del fichero externo
     * @return los ajustes leídos
     * @throws RuntimeException si no se puede leer el fichero
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();
        Path externalFile = Path.of(PROPERTIES_FILE);
        try (InputStream inputStream = Files.isRegularFile(externalFile)
                ? Files.newInputStream(externalFile)
                : DatasourceFactory.class.getResourceAsStream("/" + PROPERTIES_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not read " + PROPERTIES_FILE, exception);
        }
        return properties;
    }

    /**
     * Busca un ajuste  entre las propiedades del sistema, después entre
     * las variables  y por luegoi en el fichero de configuración.
     *
     * @param propertyName    nombre del ajuste en las propiedades del sistema o en el fichero
     * @param environmentName nombre de la variable
     * @param properties      ajustes leídos del fichero
     * @return el valor encontrado o {@code defaultValue} si no hay ninguno
     */
    private static String setting(String propertyName, String environmentName,
                                  Properties properties, String defaultValue) {
        String systemValue = System.getProperty(propertyName);
        if (systemValue != null) {
            return systemValue;
        }
        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null) {
            return environmentValue;
        }
        return properties.getProperty(propertyName, defaultValue);
    }
}
