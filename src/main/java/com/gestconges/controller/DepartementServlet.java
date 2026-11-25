package com.gestconges.controller;

import com.gestconges.model.Departement;
import com.gestconges.model.Employe;
import com.gestconges.service.DepartementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/admin/departments", "/admin/departments/*"})
public class DepartementServlet extends HttpServlet {

    private DepartementService departementService;

    @Override
    public void init() {
        departementService = new DepartementService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            renderList(request, response);
        } else if ("/new".equals(path)) {
            renderForm(request, response, null);
        } else if ("/edit".equals(path)) {
            Long id = parseRequestId(request, response);
            if (id == null) {
                return;
            }
            Departement department = departementService.findById(id);
            if (department == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            renderForm(request, response, department);
        } else if ("/team".equals(path)) {
            Long id = parseRequestId(request, response);
            if (id == null) {
                return;
            }
            if (departementService.findById(id) == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            renderTeam(request, response, id);
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
        String path = request.getPathInfo();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            createDepartment(request, response);
        } else if ("/update".equals(path)) {
            updateDepartment(request, response);
        } else if ("/delete".equals(path)) {
            deleteDepartment(request, response);
        } else if ("/assign-manager".equals(path)) {
            assignManager(request, response);
        } else if ("/unassign-manager".equals(path)) {
            unassignManager(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void createDepartment(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        try {
            departementService.createDepartment(
                    request.getParameter("nom"),
                    request.getParameter("description")
            );
            redirectToList(request, response, "created");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            renderForm(request, response, null);
        }
    }

    private void updateDepartment(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Long id = parseRequestId(request, response);
        if (id == null) {
            return;
        }
        try {
            departementService.updateDepartment(
                    id,
                    request.getParameter("nom"),
                    request.getParameter("description")
            );
            redirectToList(request, response, "updated");
        } catch (IllegalArgumentException exception) {
            Departement department = departementService.findById(id);
            if (department == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("error", exception.getMessage());
            renderForm(request, response, department);
        }
    }

    private void deleteDepartment(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Long id = parseRequestId(request, response);
        if (id == null) {
            return;
        }
        try {
            departementService.deleteDepartment(id);
            redirectToList(request, response, "deleted");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            renderList(request, response);
        }
    }

    private void assignManager(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Long departmentId = parseRequestId(request, response);
        if (departmentId == null) {
            return;
        }
        try {
            departementService.assignManager(
                    departmentId,
                    parseId(request.getParameter("managerId")),
                    parseIds(request.getParameterValues("employeeIds"))
            );
            redirectToTeam(request, response, departmentId, "assigned");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            renderTeam(request, response, departmentId);
        }
    }

    private void unassignManager(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Long departmentId = parseRequestId(request, response);
        if (departmentId == null) {
            return;
        }
        try {
            departementService.unassignManager(
                    departmentId,
                    parseIds(request.getParameterValues("employeeIds"))
            );
            redirectToTeam(request, response, departmentId, "unassigned");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            renderTeam(request, response, departmentId);
        }
    }

    private void renderList(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        request.setAttribute("departements", departementService.findAll());
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/departments/list.jsp"
        ).forward(request, response);
    }

    private void renderForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Departement department
    ) throws ServletException, IOException {
        request.setAttribute("departement", department);
        request.setAttribute("isEdit", department != null);
        request.setAttribute(
                "formTitle",
                department == null
                        ? "Créer un département"
                        : "Modifier un département"
        );
        request.setAttribute(
                "formAction",
                request.getContextPath()
                        + (department == null
                        ? "/admin/departments"
                        : "/admin/departments/update")
        );
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/departments/form.jsp"
        ).forward(request, response);
    }

    private void renderTeam(
            HttpServletRequest request,
            HttpServletResponse response,
            Long departmentId
    ) throws ServletException, IOException {
        Departement department = departementService.findById(departmentId);
        if (department == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        request.setAttribute("departement", department);
        request.setAttribute(
                "employes",
                departementService.findEmployees(departmentId)
        );
        request.setAttribute(
                "responsables",
                departementService.findManagerCandidates(departmentId)
        );
        request.setAttribute("departmentId", departmentId);
        request.setAttribute(
                "selectedManagerId",
                request.getParameter("managerId")
        );
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/departments/team.jsp"
        ).forward(request, response);
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

    private List<Long> parseIds(String[] values) {
        List<Long> ids = new ArrayList<>();
        if (values == null) {
            return ids;
        }
        for (String value : values) {
            ids.add(parseId(value));
        }
        return ids;
    }

    private void redirectToList(
            HttpServletRequest request,
            HttpServletResponse response,
            String status
    ) throws IOException {
        response.sendRedirect(
                request.getContextPath()
                        + "/admin/departments?status="
                        + status
        );
    }

    private void redirectToTeam(
            HttpServletRequest request,
            HttpServletResponse response,
            Long departmentId,
            String status
    ) throws IOException {
        response.sendRedirect(
                request.getContextPath()
                        + "/admin/departments/team?id="
                        + departmentId
                        + "&status="
                        + status
        );
    }
}
