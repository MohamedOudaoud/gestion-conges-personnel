package com.gestconges.controller;

import com.gestconges.model.Employe;
import com.gestconges.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    // Display the login page
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Already authenticated?
        if (session != null && session.getAttribute("employeId") != null) {
            String dashboardPath = DashboardPaths.pathForRole(
                    (String) session.getAttribute("employeRole")
            );
            if (dashboardPath == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            response.sendRedirect(
                    request.getContextPath() + dashboardPath
            );
            return;
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/auth/login.jsp"
        ).forward(request, response);
    }

    // Process login form submission
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Authenticate user
        Employe employe = authService.authenticate(email, password);

        // Authentication failed
        if (employe == null) {
            request.setAttribute(
                    "error",
                    "Email ou mot de passe incorrect."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/auth/login.jsp"
            ).forward(request, response);

            return;
        }

        // Authentication successful:
        // Invalidate the previous session to prevent session fixation.
        HttpSession oldSession = request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        // Create a new authenticated session
        HttpSession session = request.getSession(true);

        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Store authenticated employee information
        session.setAttribute("employeId", employe.getId());
        session.setAttribute("employeRole", employe.getRole().name());
        session.setAttribute("employeNom", employe.getNom());
        session.setAttribute("employePrenom", employe.getPrenom());

        String dashboardPath = DashboardPaths.pathForRole(
                employe.getRole().name()
        );
        if (dashboardPath == null) {
            session.invalidate();
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Redirect to the dashboard for the authenticated role.
        response.sendRedirect(
                request.getContextPath() + dashboardPath
        );
    }
}
