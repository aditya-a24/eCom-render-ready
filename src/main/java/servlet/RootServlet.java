package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Mapped to "/" so that a plain GET to the context root is forwarded to
 * index.jsp without relying on Tomcat's welcome-file resolution, which can
 * race against WAR unpacking during startup.
 *
 * Using a forward (not redirect) means the browser URL stays at "/" while
 * index.jsp is rendered — cleaner UX and no extra round-trip.
 */
public class RootServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
