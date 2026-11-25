package com.gestconges.controller;

import com.gestconges.model.TypeConge;
import com.gestconges.service.TypeCongeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/admin/leave-types", "/admin/leave-types/*"})
public class TypeCongeServlet extends HttpServlet {

    private TypeCongeService typeCongeService;

    @Override
    public void init() {
        typeCongeService = new TypeCongeService();
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
            TypeConge typeConge = typeCongeService.findById(id);
            if (typeConge == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            renderForm(request, response, typeConge);
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
            createTypeConge(request, response);
        } else if ("/update".equals(path)) {
            updateTypeConge(request, response);
        } else if ("/status".equals(path)) {
            updateStatus(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void createTypeConge(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        try {
            typeCongeService.createTypeConge(
                    request.getParameter("libelle"),
                    parseJoursParAn(request.getParameter("joursParAn")),
                    isChecked(request, "decompteSolde"),
                    isChecked(request, "justificatifRequis")
            );
            redirectToList(request, response, "created");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            renderForm(request, response, null);
        }
    }

    private void updateTypeConge(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Long id = parseRequestId(request, response);
        if (id == null) {
            return;
        }
        try {
            typeCongeService.updateTypeConge(
                    id,
                    request.getParameter("libelle"),
                    parseJoursParAn(request.getParameter("joursParAn")),
                    isChecked(request, "decompteSolde"),
                    isChecked(request, "justificatifRequis")
            );
            redirectToList(request, response, "updated");
        } catch (IllegalArgumentException exception) {
            TypeConge typeConge = typeCongeService.findById(id);
            if (typeConge == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("error", exception.getMessage());
            renderForm(request, response, typeConge);
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

        boolean active = Boolean.parseBoolean(activeValue);
        try {
            typeCongeService.setActive(id, active);
            redirectToList(
                    request,
                    response,
                    active ? "activated" : "deactivated"
            );
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            renderList(request, response);
        }
    }

    private void renderList(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        request.setAttribute("typesConge", typeCongeService.findAll());
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/leave-types/list.jsp"
        ).forward(request, response);
    }

    private void renderForm(
            HttpServletRequest request,
            HttpServletResponse response,
            TypeConge typeConge
    ) throws ServletException, IOException {
        boolean submitted = "POST".equalsIgnoreCase(request.getMethod());
        request.setAttribute("typeConge", typeConge);
        request.setAttribute(
                "formLibelle",
                submitted
                        ? request.getParameter("libelle")
                        : typeConge == null ? "" : typeConge.getLibelle()
        );
        request.setAttribute(
                "formJoursParAn",
                submitted
                        ? request.getParameter("joursParAn")
                        : typeConge == null || typeConge.getJoursParAn() == null
                        ? ""
                        : typeConge.getJoursParAn()
        );
        request.setAttribute(
                "formDecompteSolde",
                submitted
                        ? isChecked(request, "decompteSolde")
                        : typeConge != null && typeConge.isDecompteSolde()
        );
        request.setAttribute(
                "formJustificatifRequis",
                submitted
                        ? isChecked(request, "justificatifRequis")
                        : typeConge != null && typeConge.isJustificatifRequis()
        );
        request.setAttribute("isEdit", typeConge != null);
        request.setAttribute(
                "formTitle",
                typeConge == null
                        ? "Créer un type de congé"
                        : "Modifier un type de congé"
        );
        request.setAttribute(
                "formAction",
                request.getContextPath()
                        + (typeConge == null
                        ? "/admin/leave-types"
                        : "/admin/leave-types/update")
        );
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/leave-types/form.jsp"
        ).forward(request, response);
    }

    private boolean isChecked(HttpServletRequest request, String parameter) {
        return "true".equals(request.getParameter(parameter));
    }

    private Integer parseJoursParAn(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Le nombre de jours par an est invalide."
            );
        }
    }

    private Long parseRequestId(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            long id = Long.parseLong(request.getParameter("id"));
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException exception) {
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
                        + "/admin/leave-types?status="
                        + status
        );
    }
}
