package com.gestconges.controller;

import com.gestconges.model.DemandeConge;
import com.gestconges.model.Role;
import com.gestconges.service.DemandeCongeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {
        "/manager/leave-requests",
        "/manager/leave-requests/*",
        "/admin/leave-requests",
        "/admin/leave-requests/*"
})
public class ApprovalCongeServlet extends HttpServlet {

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
        ValidatorSession validator = validatorSession(request, response);
        if (validator == null) {
            return;
        }

        String path = request.getPathInfo();
        if (path != null && !path.isEmpty() && !"/".equals(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        List<DemandeConge> pendingRequests;
        if (Role.ADMIN.name().equals(validator.role)) {
            pendingRequests = demandeCongeService.findAllPending();
        } else {
            pendingRequests =
                    demandeCongeService.findPendingForManager(
                            validator.employeeId
                    );
        }

        request.setAttribute("demandes", pendingRequests);
        request.setAttribute("validatorRole", validator.role);
        request.getRequestDispatcher(
                "/WEB-INF/views/approval/leave-requests.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        ValidatorSession validator = validatorSession(request, response);
        if (validator == null) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String path = request.getPathInfo();
        if ("/approve".equals(path) || "/reject".equals(path)) {
            Long requestId = parseId(
                    request.getParameter("id"),
                    response
            );
            if (requestId == null) {
                return;
            }

            try {
                if ("approve".equals(normalizedPath(path))) {
                    demandeCongeService.approveRequest(
                            requestId,
                            validator.employeeId
                    );
                } else {
                    demandeCongeService.rejectRequest(
                            requestId,
                            validator.employeeId,
                            request.getParameter("commentaireDecision")
                    );
                }
                redirectToList(request, response, validator.role);
            } catch (IllegalArgumentException exception) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        exception.getMessage()
                );
            } catch (SecurityException exception) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        exception.getMessage()
                );
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private ValidatorSession validatorSession(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null
                || session.getAttribute("employeId") == null
                || session.getAttribute("employeRole") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        Object employeeIdValue = session.getAttribute("employeId");
        if (!(employeeIdValue instanceof Long)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        String role = session.getAttribute("employeRole").toString();
        if (!Role.RESPONSABLE.name().equals(role)
                && !Role.ADMIN.name().equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return new ValidatorSession((Long) employeeIdValue, role);
    }

    private Long parseId(
            String value,
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
            String role
    ) throws IOException {
        String prefix = Role.ADMIN.name().equals(role)
                ? "/admin/leave-requests"
                : "/manager/leave-requests";
        response.sendRedirect(
                request.getContextPath() + prefix + "?status=updated"
        );
    }

    private static class ValidatorSession {
        private final Long employeeId;
        private final String role;

        private ValidatorSession(Long employeeId, String role) {
            this.employeeId = employeeId;
            this.role = role;
        }
    }
}
