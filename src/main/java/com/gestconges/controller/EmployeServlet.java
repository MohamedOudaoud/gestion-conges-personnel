package com.gestconges.controller;

import com.gestconges.model.Employe;
import com.gestconges.model.Role;
import com.gestconges.service.EmployeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

@WebServlet(urlPatterns = {"/admin/employees", "/admin/employees/*"})
public class EmployeServlet extends HttpServlet {

    private EmployeService employeService;

    @Override
    public void init() {
        employeService = new EmployeService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            request.setAttribute("employes", employeService.findAll());
            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/employees/list.jsp"
            ).forward(request, response);
            return;
        }

        if ("/new".equals(path)) {
            renderForm(request, response, null);
            return;
        }

        if ("/edit".equals(path)) {
            Long id = parseRequestId(request, response);
            if (id == null) {
                return;
            }
            Employe employe = employeService.findById(id);
            if (employe == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            renderForm(request, response, employe);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getPathInfo();

        if (path == null || path.isEmpty() || "/".equals(path)) {
            createEmployee(request, response);
        } else if ("/update".equals(path)) {
            updateEmployee(request, response);
        } else if ("/status".equals(path)) {
            updateStatus(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void createEmployee(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        try {
            employeService.createEmployee(
                    request.getParameter("matricule"),
                    request.getParameter("nom"),
                    request.getParameter("prenom"),
                    request.getParameter("email"),
                    request.getParameter("initialPassword"),
                    parseRole(request.getParameter("role")),
                    parseDate(request.getParameter("dateEmbauche")),
                    parseId(request.getParameter("departementId"))
            );
            redirectToList(request, response, "created");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            renderForm(request, response, null);
        }
    }

    private void updateEmployee(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Long id = parseRequestId(request, response);
        if (id == null) {
            return;
        }
        try {
            Role role = parseRole(request.getParameter("role"));
            employeService.updateEmployee(
                    id,
                    request.getParameter("matricule"),
                    request.getParameter("nom"),
                    request.getParameter("prenom"),
                    request.getParameter("email"),
                    role,
                    parseDate(request.getParameter("dateEmbauche")),
                    parseId(request.getParameter("departementId"))
            );

            HttpSession session = request.getSession(false);
            if (session != null
                    && id.equals(session.getAttribute("employeId"))) {
                session.setAttribute(
                        "employeNom",
                        request.getParameter("nom").trim()
                );
                session.setAttribute(
                        "employePrenom",
                        request.getParameter("prenom").trim()
                );
                session.setAttribute("employeRole", role.name());
                response.sendRedirect(
                        request.getContextPath()
                                + DashboardPaths.pathForRole(role.name())
                );
                return;
            }
            redirectToList(request, response, "updated");
        } catch (IllegalArgumentException exception) {
            Employe employe = employeService.findById(id);
            if (employe == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("error", exception.getMessage());
            renderForm(request, response, employe);
        }
    }

    private void updateStatus(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Long id = parseRequestId(request, response);
        if (id == null) {
            return;
        }
        String activeValue = request.getParameter("active");
        if (!"true".equals(activeValue) && !"false".equals(activeValue)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Employe employe = employeService.findById(id);
        if (employe == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean active = Boolean.parseBoolean(activeValue);
        employeService.setActive(id, active);
        HttpSession session = request.getSession(false);
        if (!active && session != null
                && id.equals(session.getAttribute("employeId"))) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        redirectToList(
                request,
                response,
                active ? "activated" : "deactivated"
        );
    }

    private void renderForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Employe employe
    ) throws ServletException, IOException {
        request.setAttribute("employe", employe);
        request.setAttribute(
                "departements",
                employeService.findAllDepartements()
        );
        request.setAttribute(
                "roles",
                Arrays.stream(Role.values()).map(Role::name).toList()
        );
        request.setAttribute(
                "currentRole",
                employe == null ? "" : employe.getRole().name()
        );
        request.setAttribute(
                "currentDepartmentId",
                employe == null || employe.getDepartement() == null
                        ? ""
                        : employe.getDepartement().getId().toString()
        );
        request.setAttribute("isEdit", employe != null);
        request.setAttribute(
                "formTitle",
                employe == null ? "Créer un employé" : "Modifier un employé"
        );
        request.setAttribute(
                "formAction",
                request.getContextPath()
                        + (employe == null
                        ? "/admin/employees"
                        : "/admin/employees/update")
        );
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/employees/form.jsp"
        ).forward(request, response);
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Veuillez choisir un rôle.");
        }
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "La date d'embauche est invalide."
            );
        }
    }

    private Long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "L'identifiant fourni est invalide."
            );
        }
    }

    private Long parseRequestId(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            return parseId(request.getParameter("id"));
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    private void redirectToList(
            HttpServletRequest request,
            HttpServletResponse response,
            String status
    ) throws IOException {
        response.sendRedirect(
                request.getContextPath()
                        + "/admin/employees?status="
                        + status
        );
    }
}
