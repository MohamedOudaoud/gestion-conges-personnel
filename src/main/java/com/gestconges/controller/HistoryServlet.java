package com.gestconges.controller;

import com.gestconges.model.DemandeConge;
import com.gestconges.model.Role;
import com.gestconges.model.StatutDemande;
import com.gestconges.service.HistoryDashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(urlPatterns = {
        "/employee/leave-history",
        "/employee/leave-history/*",
        "/manager/leave-history",
        "/manager/leave-history/*",
        "/admin/leave-history",
        "/admin/leave-history/*"
})
public class HistoryServlet extends HttpServlet {

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
        HttpSession session = request.getSession(false);
        if (session == null
                || session.getAttribute("employeId") == null
                || session.getAttribute("employeRole") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String role = session.getAttribute("employeRole").toString();
        Long employeeId = (Long) session.getAttribute("employeId");
        String requestUri = request.getRequestURI()
                .substring(request.getContextPath().length());

        if (requestUri.startsWith("/employee/")
                && !Role.EMPLOYE.name().equals(role)
                && !Role.RESPONSABLE.name().equals(role)
                && !Role.ADMIN.name().equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (requestUri.startsWith("/manager/")
                && !Role.RESPONSABLE.name().equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (requestUri.startsWith("/admin/")
                && !Role.ADMIN.name().equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            StatutDemande status = HistoryDashboardService.parseStatus(
                    request.getParameter("status")
            );
            Long leaveTypeId = parseOptionalId(
                    request.getParameter("typeCongeId")
            );
            LocalDate startDate = parseOptionalDate(
                    request.getParameter("dateDebut")
            );
            LocalDate endDate = parseOptionalDate(
                    request.getParameter("dateFin")
            );
            HistoryDashboardService.validateFilterDates(
                    startDate,
                    endDate
            );

            List<DemandeConge> history;
            if (requestUri.startsWith("/employee/")) {
                history = historyDashboardService.findPersonalHistory(
                        employeeId,
                        status == null ? "" : status.name(),
                        leaveTypeId,
                        startDate,
                        endDate
                );
            } else if (requestUri.startsWith("/manager/")) {
                history = historyDashboardService.findTeamHistory(
                        employeeId,
                        status == null ? "" : status.name(),
                        leaveTypeId,
                        startDate,
                        endDate
                );
            } else {
                history = historyDashboardService.findAllHistory(
                        status == null ? "" : status.name(),
                        leaveTypeId,
                        startDate,
                        endDate
                );
            }

            request.setAttribute("demandes", history);
            request.setAttribute(
                    "typesConge",
                    historyDashboardService.findLeaveTypes()
            );
            request.setAttribute(
                    "statuses",
                    StatutDemande.values()
            );
            request.setAttribute("selectedStatus",
                    status == null ? "" : status.name());
            request.setAttribute(
                    "selectedTypeId",
                    request.getParameter("typeCongeId")
            );
            request.setAttribute(
                    "selectedDateDebut",
                    request.getParameter("dateDebut")
            );
            request.setAttribute(
                    "selectedDateFin",
                    request.getParameter("dateFin")
            );
            request.setAttribute("historyRole", role);
            request.getRequestDispatcher(
                    "/WEB-INF/views/history/leave-history.jsp"
            ).forward(request, response);
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            request.setAttribute("demandes", List.of());
            request.setAttribute(
                    "typesConge",
                    historyDashboardService.findLeaveTypes()
            );
            request.setAttribute("statuses", StatutDemande.values());
            request.setAttribute("historyRole", role);
            request.getRequestDispatcher(
                    "/WEB-INF/views/history/leave-history.jsp"
            ).forward(request, response);
        }
    }

    private LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Une date de filtre est invalide."
            );
        }
    }

    private Long parseOptionalId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Le type de congé sélectionné est invalide."
            );
        }
    }
}
