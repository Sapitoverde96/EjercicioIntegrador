package com.cesur.valciro.data.config;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DatasourceFactory {
    private static final String PROPERTIES_FILE = "database.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:32768/database";

    private static DataSource datasource = null;

    private DatasourceFactory() {}

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
