package com.cesur.valciro.data.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface BasicDAO<Type> {
    List<Type> findAll();
    List<Type> findAll(Connection connection);

    Optional<Type> findById(Long id);
    Optional<Type> findById(Connection connection, Long id);

    Type save(Type entity);
    Type save(Connection connection, Type entity);

    void update(Type entity);
    void update(Connection connection, Type entity);

    void deleteById(Long id);
    void deleteById(Connection connection, Long id);
}
