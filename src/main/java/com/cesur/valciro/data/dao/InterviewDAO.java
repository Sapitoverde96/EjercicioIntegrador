package com.cesur.valciro.data.dao;

import com.cesur.valciro.business.models.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterviewDAO implements BasicDAO<Interview> {
    private final DataSource datasource;
    private final Connection connection;

    /**
     * Consulta base que trae los datos de la entrevista junto con los de su oferta
     * y la empresa de esta. Las columnas de la oferta llevan el prefijo {@code offer_}
     * y las de la empresa {@code offer_company_}, para no confundirlas entre sí.
     */
    private static final String SELECT_INTERVIEW_OFFER_AND_COMPANY =
            "SELECT interview.id, interview.date, interview.notes, interview.type, interview.outcome, " +
                    "offer.id AS offer_id, offer.title AS offer_title, offer.status AS offer_status, " +
                    "offer.date AS offer_date, offer.link AS offer_link, offer.description AS offer_description, " +
                    "offer.notes AS offer_notes, offer.salary AS offer_salary, " +
                    "company.id AS offer_company_id, company.name AS offer_company_name, " +
                    "company.industry AS offer_company_industry, company.website AS offer_company_website, " +
                    "company.date AS offer_company_date " +
                    "FROM interview " +
                    "JOIN offer ON interview.offer_id = offer.id " +
                    "JOIN company ON offer.company_id = company.id";

    public InterviewDAO(DataSource datasource) {
        this.datasource = datasource;
        this.connection = null;
    }
    public InterviewDAO(Connection connection) {
        this.datasource = null;
        this.connection = connection;
    }

    /**
     * Devuelve todas las entrevistas guardadas, cada una con su oferta y empresa.
     *
     * @return lista de entrevistas (vacía si no hay ninguna)
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */
    @Override
    public List<Interview> findAll() {
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
     * Devuelve todas las entrevistas guardadas, cada una con su oferta y empresa,
     * usando la conexión indicada.
     *
     * @param connection conexión que se usará para la consulta
     * @return lista de entrevistas (vacía si no hay ninguna)
     * @throws RuntimeException si falla la base de datos
     */
    @Override
    public List<Interview> findAll(Connection connection) {
        List<Interview> interviews = new ArrayList<>();
        try (PreparedStatement pdStatement = connection.prepareStatement(SELECT_INTERVIEW_OFFER_AND_COMPANY); ResultSet rtSet = pdStatement.executeQuery()) {
            while (rtSet.next()) {
                interviews.add(InterviewDAO.mapper(rtSet));
            }
        } catch (SQLException exception) { throw new RuntimeException(exception); }
        return interviews;
    }
    /**
     * Busca una entrevista por su identificador.
     *
     * @param id identificador de la entrevista
     * @return la entrevista con su oferta y empresa, o un {@link Optional} vacío si no existe
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */
    @Override
    public Optional<Interview> findById(Long id) {
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
    public Optional<Interview> findById(Connection connection, Long id) {
        String sqlStatement = SELECT_INTERVIEW_OFFER_AND_COMPANY + " WHERE interview.id = ?";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement)) {
            pdStatement.setLong(1, id);
            try (ResultSet rtSet = pdStatement.executeQuery()) {
                if (rtSet.next()) {
                    return Optional.of(InterviewDAO.mapper(rtSet));
                }
            }
        } catch (SQLException exception) { throw new RuntimeException(exception); }
        return Optional.empty();
    }
    /**
     * Guarda una entrevista nueva. Al terminar, la entrevista recibida ya tiene
     * asignado el id que le ha dado la base de datos. Su oferta debe existir ya
     * (tener id).
     *
     * @param entity entrevista que se quiere guardar
     * @return la misma entrevista pero con su id
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */
    @Override
    public Interview save(Interview entity) {
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
     * Guarda una entrevista nueva usando la conexión indicada. Al terminar, la
     * entrevista recibida ya tiene asignado el id generado por la base de datos.
     *
     * @param connection conexión que se usará para guardar
     * @param entity     entrevista que se quiere guardar (su oferta debe tener id)
     * @return la misma entrevista, ya con su id
     * @throws RuntimeException si falla la base de datos
     */
    @Override
    public Interview save(Connection connection, Interview entity) {
        String sqlStatement = "INSERT INTO interview (offer_id, date, notes, type, outcome) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {
            pdStatement.setLong(1, entity.getOffer().getId());
            pdStatement.setDate(2, Date.valueOf(entity.getDate()));
            pdStatement.setString(3, entity.getNotes());
            pdStatement.setString(4, entity.getType().name().toLowerCase());
            pdStatement.setString(5, entity.getOutcome().name().toLowerCase());
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
     * Actualiza los datos de una entrevista que ya existe (se identifica por su id).
     *
     * @param entity entrevista con los nuevos datos
     * @throws RuntimeException si no hay conexión configurada o falla la base de datos
     */
    @Override
    public void update(Interview entity) {
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

    /**
     * Actualiza los datos de una entrevista que ya existe usando la conexión indicada.
     *
     * @param connection conexión que se usará para actualizar
     * @param entity     entrevista con los nuevos datos (debe tener id)
     * @throws RuntimeException si falla la base de datos
     */
    @Override
    public void update(Connection connection, Interview entity) {
        String sqlStatement = "UPDATE interview SET offer_id = ?, date = ?, notes = ?, type = ?, outcome = ? " +
                "WHERE id = ?";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement)) {
            pdStatement.setLong(1, entity.getOffer().getId());
            pdStatement.setDate(2, Date.valueOf(entity.getDate()));
            pdStatement.setString(3, entity.getNotes());
            pdStatement.setString(4, entity.getType().name().toLowerCase());
            pdStatement.setString(5, entity.getOutcome().name().toLowerCase());
            pdStatement.setLong(6, entity.getId());
            pdStatement.executeUpdate();
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    /**
     * Borra la entrevista con el id indicado.
     *
     * @param id identificador de la entrevista que se quiere borrar
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
        String sqlStatement = "DELETE FROM interview WHERE id = ?";
        try (PreparedStatement pdStatement = connection.prepareStatement(sqlStatement)) {
            pdStatement.setLong(1, id);
            pdStatement.executeUpdate();
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    /**
     * Convierte la fila actual del resultado en un objeto {@link Interview},
     * incluida su oferta (y la empresa).
     * <p>
     * Las columnas de la oferta se leen con el prefijo {@code offer_}, tal y como
     * las nombra la consulta base.
     * </p>
     *
     * @param rtSet resultado de la consulta, colocado en la fila que se quiere leer
     * @return la entrevista con los datos de esa fila
     * @throws SQLException si alguna columna no existe o no se puede leer
     */
        private static Interview mapper(ResultSet rtSet) throws SQLException {
        Interview interview = new Interview();
        interview.setId(rtSet.getLong("id"));
        interview.setOffer(OfferDAO.mapper(rtSet, "offer_"));
        interview.setDate(rtSet.getDate("date").toLocalDate());
        interview.setNotes(rtSet.getString("notes"));
        interview.setType(InterviewType.valueOf(rtSet.getString("type").toUpperCase()));
        interview.setOutcome(InterviewOutcome.valueOf(rtSet.getString("outcome").toUpperCase()));
        return interview;
    }
}