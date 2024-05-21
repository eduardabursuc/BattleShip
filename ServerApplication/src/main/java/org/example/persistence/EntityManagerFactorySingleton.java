package org.example.persistence;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * The {@code EntityManagerFactorySingleton} class provides a singleton instance
 * of the {@link jakarta.persistence.EntityManagerFactory} for accessing the
 * persistence unit.
 */
public class EntityManagerFactorySingleton {
    private static final String PERSISTENCE_UNIT_NAME = "PersistenceUnit";
    private static EntityManagerFactory emf;

    private EntityManagerFactorySingleton() {
    }

    /**
     * Retrieves the singleton instance of the EntityManagerFactory.
     *
     * @return The singleton instance of the EntityManagerFactory.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (EntityManagerFactorySingleton.class) {
                if (emf == null) {
                    emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                }
            }
        }
        return emf;
    }

    /**
     * Closes the EntityManagerFactory if it is open.
     */
    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
