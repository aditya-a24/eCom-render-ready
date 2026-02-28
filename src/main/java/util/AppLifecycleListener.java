package util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Closes the singleton EntityManagerFactory when the application is undeployed.
 * This prevents connection leaks on Render / FreeSQLDatabase.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // EMF is initialized lazily in JPAUtil; just trigger it early so any
        // misconfiguration surfaces at startup rather than on first request.
        try {
            JPAUtil.getEntityManager().close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.shutdown();
    }
}
