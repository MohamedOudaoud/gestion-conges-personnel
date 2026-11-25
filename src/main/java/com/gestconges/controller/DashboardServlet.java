package com.gestconges.controller;

import com.gestconges.model.Role;
import com.gestconges.service.HistoryDashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(urlPatterns = {"/dashboard", "/dashboard/*"})
public class DashboardServlet extends HttpServlet {

    private HistoryDashboardService historyDashboardService;

    @Override
    public void init() {
        historyDashboardService = new HistoryDashboardService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        if (historyDashboardService == null) {
            historyDashboardService = new HistoryDashboardService();
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("employeId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = (String) session.getAttribute("employeRole");
        String dashboardPath = DashboardPaths.pathForRole(role);
        if (dashboardPath == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String requestPath = request.getRequestURI()
                .substring(request.getContextPath().length());
        if (requestPath.equals("/dashboard")) {
            response.sendRedirect(request.getContextPath() + dashboardPath);
            return;
        }

        if (!requestPath.equals(dashboardPath)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Long employeeId = (Long) session.getAttribute("employeId");
        if (Role.EMPLOYE.name().equals(role)) {
            request.setAttribute(
                    "dashboardSummary",
                    historyDashboardService.getPersonalSummary(employeeId)
            );
        } else if (Role.RESPONSABLE.name().equals(role)) {
            request.setAttribute(
                    "dashboardSummary",
                    historyDashboardService.getTeamSummary(employeeId)
            );
        } else if (Role.ADMIN.name().equals(role)) {
            request.setAttribute(
                    "dashboardSummary",
                    historyDashboardService.getAdminSummary()
            );
        }

        String view = "/WEB-INF/views" + dashboardPath + ".jsp";
        request.getRequestDispatcher(view).forward(request, response);
    }
}
