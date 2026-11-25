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

/**
 * Imports the job applications stored in the initial CSV file into the database.
 * The whole file is imported inside a single transaction: if any row is invalid,
 * no data is written.
 */
public class CsvImporter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy");

    private CsvImporter() {
    }

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
