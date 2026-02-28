package util;

import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Manages the HikariCP connection pool lifecycle.
 *
 * contextInitialized  — warms up the pool so the first user request is fast.
 *                       NEVER throws: if the DB is unreachable at boot time
 *                       (e.g. FreeSQLDatabase free-tier cold start) the app
 *                       still deploys and individual requests receive a proper
 *                       HTTP 500 rather than a silent HTTP 404 caused by
 *                       Tomcat marking the context as failed.
 *
 * contextDestroyed    — drains the pool cleanly to release all connections
 *                       back to FreeSQLDatabase before the container stops.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        EntityManager em = null;
        try {
            em = JPAUtil.getEntityManager();
            // Verify DB connectivity with a lightweight native query
            em.createNativeQuery("SELECT 1").getSingleResult();
            ctx.log("[AppLifecycleListener] Database connection pool initialised successfully.");
        } catch (Exception e) {
            // LOG the error but do NOT re-throw.
            //
            // If we threw here, Tomcat would mark the entire webapp as FAILED
            // and return HTTP 404 for every request — indistinguishable from a
            // missing resource.  By catching silently, the app starts normally
            // and the first request that needs the DB will get a descriptive
            // HTTP 500 with a full stack trace in the Render logs instead.
            ctx.log("[AppLifecycleListener] WARNING: Could not connect to the database at startup. " +
                    "The application will retry on the first request. Reason: " + e.getMessage());
        } finally {
            // Always close the warm-up EntityManager, even if the query failed.
            if (em != null && em.isOpen()) {
                try { em.close(); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            JPAUtil.shutdown();
            sce.getServletContext().log("[AppLifecycleListener] Database connection pool shut down.");
        } catch (Exception e) {
            sce.getServletContext().log("[AppLifecycleListener] Error during pool shutdown: " + e.getMessage());
        }
    }
}
