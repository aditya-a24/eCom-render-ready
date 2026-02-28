package embedded;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Starts an embedded Tomcat server so the WAR can run as a plain JAR on Render.
 * Render sets the PORT environment variable; we honour it here.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // Render injects PORT; default to 8080 for local testing
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);

        // Create a connector that binds to 0.0.0.0 so Render's router can reach it
        Connector connector = new Connector();
        connector.setPort(port);
        tomcat.setConnector(connector);

        // Locate the webapp directory inside the fat JAR or on the file system
        String webappPath = getWebappPath();

        Context ctx = tomcat.addWebapp("", webappPath);

        // When running from a fat JAR, classes are on the classpath, not in
        // WEB-INF/classes. Add the JAR itself as an extra resource so Tomcat
        // can find servlets and other classes.
        WebResourceRoot resources = new StandardRoot(ctx);
        File additionWebInfClasses = new File(getJarPath());
        resources.addPreResources(
            new DirResourceSet(resources, "/WEB-INF/classes",
                               additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        tomcat.start();
        System.out.println("Server started on port " + port);
        tomcat.getServer().await();
    }

    /** Returns the absolute path to the webapp directory. */
    private static String getWebappPath() throws URISyntaxException {
        // When built by maven-shade-plugin the webapp resources are at the root
        // of the JAR; we extract to a temp dir at startup.
        // Simpler: point at src/main/webapp when running from IDE,
        // or at the unpacked location when running from the shaded JAR.
        String path = System.getProperty("webapp.dir");
        if (path != null) return path;

        // Try to find webapp next to the JAR (as deployed by Render build script)
        File jarDir = new File(Main.class.getProtectionDomain()
                                         .getCodeSource().getLocation().toURI()).getParentFile();
        File webapp = new File(jarDir, "webapp");
        if (webapp.exists()) return webapp.getAbsolutePath();

        // Fall back to src/main/webapp for IDE usage
        return "src/main/webapp";
    }

    /** Returns the location of compiled classes to add to WEB-INF/classes. */
    private static String getJarPath() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain()
                                  .getCodeSource().getLocation().toURI()).getAbsolutePath();
    }
}
