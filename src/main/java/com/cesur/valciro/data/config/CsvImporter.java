package com.cesur.valciro.data.config;

import com.cesur.valciro.business.models.Company;
import com.cesur.valciro.business.models.Offer;
import com.cesur.valciro.business.models.OfferStatus;
import com.cesur.valciro.data.dao.CompanyDAO;
import com.cesur.valciro.data.dao.OfferDAO;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CsvImporter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy");

    /**
     * Constructor privado para evitar la instanciación
     */
    private CsvImporter() {
    }

    /**
     * Importa un fichero CSV a la base de datos
     * <p>
     * Se omiten las líneas vacias y la cabecera (la que empieza por {@code id;}).
     * Si alguna fila falla, se revierte y no se guarda ninguna fila.
     * </p>
     * <p>
     * Las empresas que no existan se crean automaticamente con el sector
     * "Sin especificar".
     * </p>
     * @param datasource origen de datos desde el que se obtiene la conexión
     * @param file       ruta del fichero CSV a importar
     * @return número de filas importadas correctamente
     * @throws RuntimeException si no se puede leer el fichero, si alguna fila es
     *                          inválida o si ocurre algun error al acceder a la base de datos
     */

    public static int importFile(DataSource datasource, Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new RuntimeException("Could not read CSV file: " + file, exception);
        }

        try (Connection connection = datasource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                CompanyDAO companyDAO = new CompanyDAO(connection);
                OfferDAO offerDAO = new OfferDAO(connection);
                Map<String, Company> companies = new HashMap<>();
                for (Company company : companyDAO.findAll(connection)) {
                    companies.put(company.getName().toLowerCase(Locale.ROOT), company);
                }

                int importedRows = 0;
                for (int row = 0; row < lines.size(); row++) {
                    String line = lines.get(row).trim();
                    if (line.isEmpty() || line.toLowerCase(Locale.ROOT).startsWith("id;")) {
                        continue;
                    }
                    try {
                        importedRows += importLine(connection, companyDAO, offerDAO, companies, line, file);
                    } catch (RuntimeException exception) {
                        throw new RuntimeException(
                                "Error importing CSV row " + (row + 1) + ": " + exception.getMessage(), exception);
                    }
                }
                connection.commit();
                return importedRows;
            } catch (RuntimeException | SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not import CSV file: " + file, exception);
        }
    }

    /**
     *Valida sus campos, localiza o crea la empresa
     * asociada y guarda la oferta correspondiente.
     *
     * @param connection conexión activa
     * @param companyDAO DAO usado para guardar empresas nuevas
     * @param offerDAO   DAO usado para guardar la oferta
     * @param companies  almacenamiento de empresas indexadas por nombre en minúsculas y se
     *                   actualiza si se crea una empresa nueva
     * @param line       línea del CSV ya recortada
     * @param file       fichero de origen, se usa para anotar de donde vienen las notas de la oferta
     * @return {@code 1} si la fila se ha importado correctamente
     * @throws IllegalArgumentException si la línea tiene menos de 5 campos, si falta
     *                                  algún campo obligatorio o si el estado no es válido
     * @throws java.time.format.DateTimeParseException si la fecha no cumple el formato {@code d/M/yyyy}
     */

    private static int importLine(Connection connection, CompanyDAO companyDAO, OfferDAO offerDAO,
                                  Map<String, Company> companies, String line, Path file) {
        String[] fields = line.split(";", -1);
        if (fields.length < 5) {
            throw new IllegalArgumentException("expected 5 fields separated by ';'");
        }

        String title = fields[1].trim();
        String status = fields[2].trim();
        String companyName = fields[3].trim();
        String date = fields[4].trim();

        if (title.isEmpty() || status.isEmpty() || companyName.isEmpty() || date.isEmpty()) {
            throw new IllegalArgumentException("title, status, company and date are required");
        }

        String companyKey = companyName.toLowerCase(Locale.ROOT);
        Company company = companies.get(companyKey);
        if (company == null) {
            company = new Company();
            company.setName(companyName);
            company.setIndustry("Sin especificar");
            company.setDate(LocalDate.now());
            companyDAO.save(connection, company);
            companies.put(companyKey, company);
        }

        Offer offer = new Offer();
        offer.setTitle(title);
        offer.setCompany(company);
        offer.setStatus(OfferStatus.valueOf(status.toUpperCase(Locale.ROOT)));
        offer.setDate(LocalDate.parse(date, DATE_FORMAT));
        offer.setNotes("Importada desde " + file.getFileName());
        offerDAO.save(connection, offer);
        return 1;
    }
}
