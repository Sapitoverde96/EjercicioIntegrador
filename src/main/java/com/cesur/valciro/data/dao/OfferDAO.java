package com.cesur.valciro.data.dao;

import com.cesur.valciro.business.models.Company;
import com.cesur.valciro.business.models.Offer;
import com.cesur.valciro.business.models.OfferStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class OfferDAO implements BasicDAO<Offer> {
    private final DataSource datasource;
    private final Connection connection;

    /**
     * Consulta base que trae los datos de la oferta junto con los de su empresa.
     * Las columnas de la empresa llevan el prefijo {@code company_}, para no
     * confundirlas con las de la oferta.
     */
    private static final String SELECT_OFFER_AND_COMPANY =
            "SELECT offer.id, offer.title, offer.status, offer.date, offer.link, offer.description, offer.notes, offer.salary, " +
            "company.id AS company_id, company.name AS company_name, company.industry AS company_industry, company.website AS company_website, company.date AS company_date " +
            "FROM offer JOIN company ON offer.company_id = company.id";

    /**
     * Crea el DAO usando un origen de datos y cada consulta tiene su propia conexión.
     *
     */
    public OfferDAO(DataSource datasource) {
        this.datasource = datasource;
        this.connection = null;
    }

    /**
     * Crea el DAO con una conexion ya abierta
     *
     * @param connection conexion que se usara para todas las operaciones
     */
    public OfferDAO(Connection connection) {
        this.datasource = null;
        this.connection = connection;
    }

    /**
     * Devuelve todas las ofertas guardadas cada una con su empresa
     *
     * @return lista de ofertas (sera vacía si no hay ninguna)
     * @throws RuntimeException si no hay conexión  o falla la base de datos
     */
    @Override
    public List<Offer> findAll() {
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
     * Devuelve todas las ofertas guardadas, cada una con su empresa y su conexion
     *
     * @param connection conexión que se usará para la consulta
     * @return lista de ofertas (vacia si no hay ninguna)
     * @throws RuntimeException si falla la base de datos
     */
    @Override
    public List<Offer> findAll(Connection connection) {
        List<Offer> offers = new ArrayList<>();
        try (PreparedStatement pdStatement = connection.prepareStatement(SELECT_OFFER_AND_COMPANY); ResultSet rtSet = pdStatement.executeQuery()) {
            while (rtSet.next()) {
                offers.add(OfferDAO.mapper(rtSet));
            }
        } catch (SQLException exception) { throw new RuntimeException(exception); }
        return offers;
    }

    /**
     * Busca una oferta por su identificador.
     *
     * @param id identificador de la oferta
     * @return la oferta con su empresa, o un {@link Optional} vacío si no existe
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */
    @Override
    public Optional<Offer> findById(Long id) {
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

    /**
     * Busca una oferta por su identificador usando la conexión
     *
     * @param connection conexión que se usars para la consulta
     * @param id         identificador de la oferta
     * @return la oferta con su empresa, o un {@link Optional} vacío si no existe
     * @throws RuntimeException si falla la base de datos
     */
    @Override
    public Optional<Offer> findById(Connection connection, Long id) {
        String sqlStatement = SELECT_OFFER_AND_COMPANY + " WHERE offer.id = ?";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement)) {
            pdStatement.setLong(1, id);
            try (ResultSet rtSet = pdStatement.executeQuery()) {
                if (rtSet.next()) {
                    return Optional.of(OfferDAO.mapper(rtSet));
                }
            }
        } catch (SQLException exception) { throw new RuntimeException(exception); }
        return Optional.empty();
    }

    /**
     * Guarda una oferta nueva. Al terminar la oferta ya tiene
     * asignado el id que le ha dado la base de datos.
     *
     * @param entity oferta que se quiere guardar
     * @return la misma oferta pero con  id
     * @throws RuntimeException si falla la base de datos
     */
    @Override
    public Offer save(Offer entity) {
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

    /**
     * Guarda una oferta nueva usando la conexión indicada
     *
     * @param connection conexión que se usará para guardar
     * @param entity     oferta que se quiere guardar (su empresa debe tener id)
     * @return la misma oferta, ya con su id
     * @throws RuntimeException si falla la base de datos
     */
    @Override
    public Offer save(Connection connection, Offer entity) {
        String sqlStatement = "INSERT INTO offer (title, company_id, status, date, link, description, notes, salary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {
            pdStatement.setString(1, entity.getTitle());
            pdStatement.setLong(2, entity.getCompany().getId());
            pdStatement.setString(3, entity.getStatus().name().toLowerCase());
            pdStatement.setDate(4, Date.valueOf(entity.getDate()));
            pdStatement.setString(5, entity.getLink());
            pdStatement.setString(6, entity.getDescription());
            pdStatement.setString(7, entity.getNotes());
            pdStatement.setObject(8, entity.getSalary());
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
     * Actualiza los datos de una oferta que ya existe a (se identificpor su id).
     *
     * @param entity oferta con los nuevos datos
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */
    @Override
    public void update(Offer entity) {
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
    public void update(Connection connection, Offer entity) {
        String sqlStatement = "UPDATE offer SET title = ?, company_id = ?, status = ?, date = ?, link = ?, description = ?, notes = ?, salary = ? " +
                "WHERE id = ?";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement)) {
            pdStatement.setString(1, entity.getTitle());
            pdStatement.setLong(2, entity.getCompany().getId());
            pdStatement.setString(3, entity.getStatus().name().toLowerCase());
            pdStatement.setDate(4, Date.valueOf(entity.getDate()));
            pdStatement.setString(5, entity.getLink());
            pdStatement.setString(6, entity.getDescription());
            pdStatement.setString(7, entity.getNotes());
            pdStatement.setObject(8, entity.getSalary());
            pdStatement.setLong(9, entity.getId());
            pdStatement.executeUpdate();
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    /**
     * Borra la oferta con el id indicado.
     *
     * @param id identificador de la oferta que se quiere borrar
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
        String sqlStatement = "DELETE FROM offer WHERE id = ?";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement)) {
            pdStatement.setLong(1, id);
            pdStatement.executeUpdate();
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    /**
     * Convierte la fila actual del resultado en un objeto {@link Offer},
     * incluida su empresa.
     *
     * @param rtSet resultado de la consulta
     * @param table prefijo que llevan los nombres de las columnas (puede ser vacío)
     * @return la oferta con los datos de esa fila
     * @throws SQLException si alguna columna no existe
     */
    static Offer mapper(ResultSet rtSet, String table) throws SQLException {
        Offer offer = new Offer();
        offer.setId(rtSet.getLong(table + "id"));
        offer.setTitle(rtSet.getString(table + "title"));
        offer.setCompany(CompanyDAO.mapper(rtSet, table + "company_"));
        offer.setStatus(OfferStatus.valueOf(rtSet.getString(table + "status").toUpperCase()));
        offer.setDate(rtSet.getDate(table + "date").toLocalDate());
        offer.setLink(rtSet.getString(table + "link"));
        offer.setDescription(rtSet.getString(table + "description"));
        offer.setNotes(rtSet.getString(table + "notes"));
        double salary = rtSet.getDouble(table + "salary");
        offer.setSalary(rtSet.wasNull() ? null : salary);
        return offer;
    }

    static Offer mapper(ResultSet rtSet) throws SQLException {
        return mapper(rtSet, "");
    }
}