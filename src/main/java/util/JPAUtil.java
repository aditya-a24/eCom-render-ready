package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton EntityManagerFactory.
 *
 * One EMF is created when the class is first loaded; it owns the HikariCP
 * connection pool for the lifetime of the application.  Every caller gets
 * a lightweight EntityManager from that single pool — no new connections are
 * opened per request.
 *
 * Callers MUST close() the EntityManager in a finally block:
 *
 *   EntityManager em = JPAUtil.getEntityManager();
 *   try {
 *       // ... work ...
 *   } finally {
 *       if (em != null && em.isOpen()) em.close();
 *   }
 */
public class JPAUtil {

    private static final EntityManagerFactory EMF = buildEMF();

    private JPAUtil() {}   // static-only utility class

    private static EntityManagerFactory buildEMF() {
        Map<String, String> overrides = new HashMap<>();

        String url  = System.getenv("DB_URL");
        String user = System.getenv("DB_USERNAME");
        String pass = System.getenv("DB_PASSWORD");

        if (url  != null && !url.trim().isEmpty()) {
            // Standard JPA keys (read by Hibernate before it hands off to Hikari)
            overrides.put("jakarta.persistence.jdbc.url",      url.trim());
            // Also set the Hikari-specific key so HikariCP sees it directly
            overrides.put("hibernate.hikari.jdbcUrl",          url.trim());
        }
        if (user != null && !user.trim().isEmpty()) {
            overrides.put("jakarta.persistence.jdbc.user",     user.trim());
            overrides.put("hibernate.hikari.username",         user.trim());
        }
        if (pass != null && !pass.trim().isEmpty()) {
            overrides.put("jakarta.persistence.jdbc.password", pass.trim());
            overrides.put("hibernate.hikari.password",         pass.trim());
        }

        return Persistence.createEntityManagerFactory("myPersistenceUnit", overrides);
    }

    /** Returns a fresh EntityManager backed by the shared HikariCP pool. */
    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    /** Called by AppLifecycleListener on context destroy to drain the pool. */
    public static void shutdown() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }
}
