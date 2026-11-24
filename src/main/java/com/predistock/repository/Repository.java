package com.predistock.repository;

import java.util.List;

/**
 * Generic repository interface defining data access contract.
 * Demonstrates:
 * - Interface
 * - Generics (<T>)
 * - Abstraction of persistence layer
 *
 * @param <T> Entity type managed by this repository
 */
public interface Repository<T> {

    /**
     * Saves a new entity to the database.
     *
     * @param entity the entity to persist
     */
    void save(T entity);

    /**
     * Finds an entity by its primary key ID.
     *
     * @param id primary key ID
     * @return found entity or null if not found
     */
    T findById(int id);

    /**
     * Retrieves all entities.
     *
     * @return list of entities
     */
    List<T> findAll();

    /**
     * Updates an existing entity in the database.
     *
     * @param entity the entity to update
     */
    void update(T entity);

    /**
     * Deletes an entity by its primary key ID.
     *
     * @param id primary key ID
     */
    void delete(int id);
}

