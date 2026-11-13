package com.gestconges.dao;

import org.hibernate.Session;

import java.util.List;

public class GenericDAO<T> {

    private final Class<T> entityClass;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // Retrieve an entity by its ID
    public T findById(Session session, Long id) {
        return session.get(entityClass, id);
    }

    // Retrieve all entities
    public List<T> findAll(Session session) {
        String hql = "FROM " + entityClass.getSimpleName();

        return session.createQuery(hql, entityClass)
                .getResultList();
    }

    // Insert a new entity
    public void save(Session session, T entity) {
        session.persist(entity);
    }

    // Update an existing entity
    public T update(Session session, T entity) {
        return session.merge(entity);
    }

    // Delete an entity
    public void delete(Session session, T entity) {
        T managedEntity = session.contains(entity)
                ? entity
                : session.merge(entity);

        session.remove(managedEntity);
    }
}