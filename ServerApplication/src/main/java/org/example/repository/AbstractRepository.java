package org.example.repository;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.example.persistence.EntityManagerFactorySingleton;

public abstract class AbstractRepository<T, ID extends Serializable> {
    private static final Logger LOGGER = Logger.getLogger(AbstractRepository.class.getName());

    final EntityManagerFactory entityManagerFactory;

    public AbstractRepository() {
        entityManagerFactory = EntityManagerFactorySingleton.getEntityManagerFactory();
    }

    public T findById(Class<T> entityClass, ID id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return entityManager.find(entityClass, id);
    }

    public List<T> findAll(Class<T> entityClass) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return entityManager.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass).getResultList();
    }

    public void create(T entity) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Error occurred while creating entity: " + e.getMessage());
        }
    }

}
