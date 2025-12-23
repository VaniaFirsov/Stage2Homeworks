package ru.firsov.dao;

import java.util.List;
import java.util.Optional;

public interface DAO<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    void delete(ID id);
    boolean existsById(ID id);
}
