package com.gestconges.controller;

import com.gestconges.model.DemandeConge;
import com.gestconges.service.DemandeCongeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet(urlPatterns = {
        "/employee/leave-requests",
        "/employee/leave-requests/*"
})
public class DemandeCongeServlet extends HttpServlet {

    private DemandeCongeService demandeCongeService;

    @Override
    public void init() {
        demandeCongeService = new DemandeCongeService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Long employeeId = employeeId(request, response);
        if (employeeId == null) {
            return;
        }

        String path = request.getPathInfo();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            renderList(request, response, employeeId);
        } else if ("new".equals(normalizedPath(path))) {
            renderForm(request, response, employeeId, null);
        } else if ("edit".equals(normalizedPath(path))) {
            Long requestId = parseId(
                    request.getParameter("id"),
                    request,
                    response
            );
            if (requestId == null) {
                return;
            }
            try {
                DemandeConge leaveRequest =
                        demandeCongeService.findByIdForEmployee(
                                requestId,
                                employeeId
                        );
                if (leaveRequest == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                renderForm(
                        request,
                        response,
                        employeeId,
                        leaveRequest
                );
            } catch (SecurityException exception) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Long employeeId = employeeId(request, response);
        if (employeeId == null) {
            return;
        }

        String path = request.getPathInfo();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            createRequest(request, response, employeeId);
        } else if ("update".equals(normalizedPath(path))) {
            updateRequest(request, response, employeeId);
        } else if ("cancel".equals(normalizedPath(path))) {
            cancelRequest(request, response, employeeId);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void renderList(
            HttpServletRequest request,
            HttpServletResponse response,
            Long employeeId
    ) throws ServletException, IOException {
        request.setAttribute(
                "demandes",
                demandeCongeService.findByEmployee(employeeId)
        );
        request.getRequestDispatcher(
                "/WEB-INF/views/employee/leave-requests/list.jsp"
        ).forward(request, response);
    }

    private void renderForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Long employeeId,
            DemandeConge leaveRequest
    ) throws ServletException, IOException {
        request.setAttribute("demande", leaveRequest);
        request.setAttribute(
                "typesConge",
                demandeCongeService.findActiveTypes()
        );
        boolean submitted = "POST".equalsIgnoreCase(request.getMethod());
        request.setAttribute(
                "formTypeId",
                submitted
                        ? request.getParameter("typeCongeId")
                        : leaveRequest == null
                        ? ""
                        : leaveRequest.getTypeConge().getId().toString()
        );
        request.setAttribute(
                "formDateDebut",
                submitted
                        ? request.getParameter("dateDebut")
                        : leaveRequest == null
                        ? ""
                        : leaveRequest.getDateDebut().toString()
        );
        request.setAttribute(
                "formDateFin",
                submitted
                        ? request.getParameter("dateFin")
                        : leaveRequest == null
                        ? ""
                        : leaveRequest.getDateFin().toString()
        );
        request.setAttribute(
                "formMotif",
                submitted
                        ? request.getParameter("motif")
                        : leaveRequest == null
                        ? ""
                        : leaveRequest.getMotif()
        );
        request.setAttribute("isEdit", leaveRequest != null);
        request.setAttribute(
                "formTitle",
                leaveRequest == null
                        ? "Nouvelle demande de congé"
                        : "Modifier la demande"
        );
        request.setAttribute(
                "formAction",
                request.getContextPath()
                        + "/employee/leave-requests"
                        + (leaveRequest == null ? "" : "/update")
        );
        request.getRequestDispatcher(
                "/WEB-INF/views/employee/leave-requests/form.jsp"
        ).forward(request, response);
    }

    private void createRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            Long employeeId
    ) throws ServletException, IOException {
        try {
            demandeCongeService.createRequest(
                    employeeId,
                    parseId(request.getParameter("typeCongeId"), request, response),
                    parseDate(request.getParameter("dateDebut")),
                    parseDate(request.getParameter("dateFin")),
                    request.getParameter("motif")
            );
            redirectToList(request, response, "created");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            renderForm(request, response, employeeId, null);
        }
    }

    private void updateRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            Long employeeId
    ) throws ServletException, IOException {
        Long requestId = parseId(
                request.getParameter("id"),
                request,
                response
        );
        if (requestId == null) {
            return;
        }

        try {
            demandeCongeService.updateRequest(
                    requestId,
                    employeeId,
                    parseId(request.getParameter("typeCongeId"), request, response),
                    parseDate(request.getParameter("dateDebut")),
                    parseDate(request.getParameter("dateFin")),
                    request.getParameter("motif")
            );
            redirectToList(request, response, "updated");
        } catch (IllegalArgumentException exception) {
            try {
                DemandeConge leaveRequest =
                        demandeCongeService.findByIdForEmployee(
                                requestId,
                                employeeId
                        );
                request.setAttribute("error", exception.getMessage());
                renderForm(
                        request,
                        response,
                        employeeId,
                        leaveRequest
                );
            } catch (SecurityException securityException) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void cancelRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            Long employeeId
    ) throws IOException {
        Long requestId = parseId(
                request.getParameter("id"),
                request,
                response
        );
        if (requestId == null) {
            return;
        }
        try {
            demandeCongeService.cancelRequest(requestId, employeeId);
            redirectToList(request, response, "cancelled");
        } catch (IllegalArgumentException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    exception.getMessage()
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private Long employeeId(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null
                || session.getAttribute("employeId") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return null;
        }
        return (Long) session.getAttribute("employeId");
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "La date saisie est invalide."
            );
        }
    }

    private Long parseId(
            String value,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    private String normalizedPath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private void redirectToList(
            HttpServletRequest request,
            HttpServletResponse response,
            String status
    ) throws IOException {
        response.sendRedirect(
                request.getContextPath()
                        + "/employee/leave-requests?status="
                        + status
        );
    }
}
