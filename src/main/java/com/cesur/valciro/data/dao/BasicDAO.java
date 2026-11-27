package com.cesur.valciro.data.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;


public interface BasicDAO<Type> {

    List<Type> findAll();

    /**
     * Devuelve todos los objetos guardados usando la conexión indicada.
     *
     * @param connection conexión que se usará para la consulta
     * @return lista de objetos (vacía si no hay ninguno)
     */
    List<Type> findAll(Connection connection);

    /**
     * Busca un objeto por su identificador.
     *
     * @param id identificador del objeto
     * @return el objeto, o un {@link Optional} vacío si no existe
     */
    Optional<Type> findById(Long id);

    Optional<Type> findById(Connection connection, Long id);

    /**
     * Guarda un objeto nuevo. Al terminar, el objeto recibido ya tiene asignado
     * el id que le ha dado la base de datos.
     *
     * @param entity objeto que se quiere guardar
     * @return el mismo objeto pero ya con su id
     */
    Type save(Type entity);

    Type save(Connection connection, Type entity);

    /**
     * Actualiza los datos de un objeto que ya existe (se identifica por su id).
     *
     * @param entity objeto con los nuevos datos
     */
    void update(Type entity);

    void update(Connection connection, Type entity);

    /**
     * Borra el objeto con el id indicado.
     *
     * @param id identificador del objeto que se quiere borrar
     */
    void deleteById(Long id);

    void deleteById(Connection connection, Long id);
}
