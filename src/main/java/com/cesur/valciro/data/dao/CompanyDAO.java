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

    /**
     * Devuelve todas las empresas guardadas usando la conexión indicada.
     *
     * @param connection conexión que se usará para la consulta
     * @return lista de empresas (vacía si no hay ninguna)
     * @throws RuntimeException si falla la base de datos
     */

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

    /**
     * Busca una empresa por su identificador.
     *
     * @param id identificador de la empresa
     * @return la empresa, o un {@link Optional} vacío si no existe
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */
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

    /**
     * Guarda una empresa nueva. Al terminar, la empresa recibida ya tiene
     * asignado el id que le ha dado la base de datos.
     *
     * @param entity empresa que se quiere guardar
     * @return la misma empresa, ya con su id
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */
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

    /**
     * Actualiza los datos de una empresa que ya existe (se identifica por su id).
     *
     * @param entity empresa con los nuevos datos
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */

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

    /**
     * Borra la empresa con el id indicado.
     *
     * @param id identificador de la empresa que se quiere borrar
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */
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

    /**
     * Convierte la fila actual del resultado en un objeto {@link Company}.
     * <p>
     * Lee las columnas de una empresa cuando vienen dentro de
     * una consulta más grande (por ejemplo, {@code "offer_company_"} si las columnas
     * se llaman {@code offer_company_name}, {@code offer_company_industry}, etc.).
     * </p>
     *
     * @param rtSet resultado de la consulta, colocado en la fila que se quiere leer
     * @param table prefijo que llevan los nombres de las columnas (puede ser vacío)
     * @return la empresa con los datos de esa fila
     * @throws SQLException si alguna columna no existe o no se puede leer
     */

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