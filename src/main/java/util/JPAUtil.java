package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton EntityManagerFactory holder.
 * DB credentials are read from environment variables at startup so that
 * no credentials are hard-coded in source or shipped in persistence.xml.
 */
public class JPAUtil {

    private static final EntityManagerFactory EMF = buildEMF();

    private static EntityManagerFactory buildEMF() {
        Map<String, String> props = new HashMap<>();

        // Prefer environment variables; fall back to persistence.xml values
        String url  = System.getenv("DB_URL");
        String user = System.getenv("DB_USERNAME");
        String pass = System.getenv("DB_PASSWORD");

        if (url  != null) props.put("jakarta.persistence.jdbc.url",      url);
        if (user != null) props.put("jakarta.persistence.jdbc.user",     user);
        if (pass != null) props.put("jakarta.persistence.jdbc.password", pass);

        return Persistence.createEntityManagerFactory("myPersistenceUnit", props);
    }

    /** Returns a new EntityManager. Caller MUST close it (use try-finally). */
    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    /** Call once on application shutdown to release all connections. */
    public static void shutdown() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }
}
