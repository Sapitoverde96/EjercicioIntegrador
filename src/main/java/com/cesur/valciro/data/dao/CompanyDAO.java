package com.cesur.valciro.data.dao;

import com.cesur.valciro.business.models.Company;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompanyDAO implements BasicDAO<Company> {
    private final DataSource datasource;
    private final Connection connection;

    public CompanyDAO(DataSource datasource) {
        this.datasource = datasource;
        this.connection = null;
    }
    public CompanyDAO(Connection connection) {
        this.datasource = null;
        this.connection = connection;
    }

    @Override
    public List<Company> findAll() {
        if (this.connection != null) {
            return findAll(this.connection);
        }
        if (this.datasource == null) {
            throw new RuntimeException("Datasource is not set");
        }
        try (Connection connection = this.datasource.getConnection()) {
            return findAll(connection);
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    @Override
    public List<Company> findAll(Connection connection) {
        String sqlStatement = "SELECT * FROM company";
        List<Company> companies = new ArrayList<>();
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement); ResultSet rtSet = pdStatement.executeQuery()) {
            while (rtSet.next()) {
                companies.add(CompanyDAO.mapper(rtSet));
            }
        } catch (SQLException exception) { throw new RuntimeException(exception); }
        return companies;
    }

    @Override
    public Optional<Company> findById(Long id) {
        if (this.connection != null) {
            return findById(this.connection, id);
        }
        if (this.datasource == null) {
            throw new RuntimeException("Datasource is not set");
        }
        try (Connection connection = this.datasource.getConnection()) {
            return findById(connection, id);
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    @Override
    public Optional<Company> findById(Connection connection, Long id) {
        String sqlStatement = "SELECT * FROM company WHERE id = ?";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement)) {
            pdStatement.setLong(1, id);
            try (ResultSet rtSet = pdStatement.executeQuery()) {
                if (rtSet.next()) {
                    return Optional.of(CompanyDAO.mapper(rtSet));
                }
            }
        } catch (SQLException exception) { throw new RuntimeException(exception); }
        return Optional.empty();
    }

    @Override
    public Company save(Company entity) {
        if (this.connection != null) {
            return save(this.connection, entity);
        }
        if (this.datasource == null) {
            throw new RuntimeException("Datasource is not set");
        }
        try (Connection connection = this.datasource.getConnection()) {
            return save(connection, entity);
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    @Override
    public Company save(Connection connection, Company entity) {
        String sqlStatement = "INSERT INTO company (name, industry, website, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {
            pdStatement.setString(1, entity.getName());
            pdStatement.setString(2, entity.getIndustry());
            pdStatement.setString(3, entity.getWebsite());
            pdStatement.setDate(4, Date.valueOf(entity.getDate()));
            pdStatement.executeUpdate();
            try (ResultSet rtSet = pdStatement.getGeneratedKeys()) {
                if (rtSet.next()) {
                    entity.setId(rtSet.getLong(1));
                }
            }
        } catch (SQLException exception) { throw new RuntimeException(exception); }
        return entity;
    }

    @Override
    public void update(Company entity) {
        if (this.connection != null) {
            update(this.connection, entity);
            return;
        }
        if (this.datasource == null) {
            throw new RuntimeException("Datasource is not set");
        }
        try (Connection connection = this.datasource.getConnection()) {
            update(connection, entity);
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    @Override
    public void update(Connection connection, Company entity) {
        String sqlStatement = "UPDATE company SET name = ?, industry = ?, website = ?, date = ? WHERE id = ?";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement)) {
            pdStatement.setString(1, entity.getName());
            pdStatement.setString(2, entity.getIndustry());
            pdStatement.setString(3, entity.getWebsite());
            pdStatement.setDate(4, Date.valueOf(entity.getDate()));
            pdStatement.setLong(5, entity.getId());
            pdStatement.executeUpdate();
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    @Override
    public void deleteById(Long id) {
        if (this.connection != null) {
            deleteById(this.connection, id);
            return;
        }
        if (this.datasource == null) {
            throw new RuntimeException("Datasource is not set");
        }
        try(Connection connection = this.datasource.getConnection()) {
            deleteById(connection, id);
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    @Override
    public void deleteById(Connection connection, Long id) {
        String sqlStatement = "DELETE FROM company WHERE id = ?";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement)) {
            pdStatement.setLong(1, id);
            pdStatement.executeUpdate();
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    static Company mapper(ResultSet rtSet, String table) throws SQLException {
        Company company = new Company();
        company.setId(rtSet.getLong(table + "id"));
        company.setName(rtSet.getString(table +"name"));
        company.setIndustry(rtSet.getString(table + "industry"));
        company.setWebsite(rtSet.getString(table + "website"));
        company.setDate(rtSet.getDate(table + "date").toLocalDate());
        return company;
    }

    static Company mapper(ResultSet rtSet) throws SQLException {
        return mapper(rtSet, "");
    }
}