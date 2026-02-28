package util;

import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Eagerly initialises the JPAUtil singleton (and therefore the HikariCP pool)
 * at application startup, and cleanly shuts it down on undeploy.
 *
 * Without this, the first HTTP request pays the cost of pool creation and any
 * DB misconfiguration only surfaces at request-time rather than at boot.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Open and immediately close one EM to force pool initialisation.
        // The try-finally guarantees the warm-up EM is always released.
        EntityManager em = null;
        try {
            em = JPAUtil.getEntityManager();
            // Execute a trivial query so Hibernate validates the schema too
            em.createNativeQuery("SELECT 1").getSingleResult();
            sce.getServletContext().log("[AppLifecycleListener] DB connection pool initialised OK.");
        } catch (Exception e) {
            // Throw so Tomcat marks the deployment as failed — misconfiguration
            // is visible immediately in Render logs instead of on the first request.
            throw new RuntimeException(
                "[AppLifecycleListener] Failed to connect to the database: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();   // ← THIS is the fix: the warm-up EM is always closed
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.shutdown();
        sce.getServletContext().log("[AppLifecycleListener] DB connection pool shut down.");
    }
}
