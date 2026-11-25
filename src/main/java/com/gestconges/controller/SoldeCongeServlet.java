package com.gestconges.controller;

import com.gestconges.model.SoldeConge;
import com.gestconges.service.SoldeCongeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Year;

@WebServlet(urlPatterns = {"/admin/balances", "/admin/balances/*"})
public class SoldeCongeServlet extends HttpServlet {

    private SoldeCongeService soldeCongeService;

    @Override
    public void init() {
        soldeCongeService = new SoldeCongeService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            renderList(request, response);
        } else if ("/adjust".equals(path)) {
            Long id = parseId(
                    request.getParameter("id"),
                    request,
                    response
            );
            if (id == null) {
                return;
            }
            SoldeConge balance = soldeCongeService.findById(id);
            if (balance == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            renderAdjustment(request, response, balance);
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
        if ("/initialize".equals(path)) {
            initializeYear(request, response);
        } else if ("/adjust".equals(path)) {
            adjustBalance(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void renderList(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        try {
            int year = parseYear(
                    request.getParameter("year"),
                    request
            );
            request.setAttribute("year", year);
            request.setAttribute(
                    "soldes",
                    soldeCongeService.findByYear(year)
            );
            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/balances/list.jsp"
            ).forward(request, response);
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            int year = Year.now().getValue();
            request.setAttribute("year", year);
            request.setAttribute(
                    "soldes",
                    soldeCongeService.findByYear(year)
            );
            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/balances/list.jsp"
            ).forward(request, response);
        }
    }

    private void initializeYear(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            int year = parseYear(
                    request.getParameter("year"),
                    request
            );
            int initializedCount =
                    soldeCongeService.initializeYear(year);
            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/balances?year="
                            + year
                            + "&status=initialized&count="
                            + initializedCount
            );
        } catch (IllegalArgumentException exception) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/balances?error="
                            + encode(exception.getMessage())
            );
        }
    }

    private void adjustBalance(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Long id = parseId(
                request.getParameter("id"),
                request,
                response
        );
        if (id == null) {
            return;
        }

        try {
            Integer acquired = parseInteger(
                    request.getParameter("joursAcquis"),
                    "Les jours acquis sont obligatoires."
            );
            Integer taken = parseInteger(
                    request.getParameter("joursPris"),
                    "Les jours pris sont obligatoires."
            );
            soldeCongeService.adjustBalance(id, acquired, taken);
            SoldeConge balance = soldeCongeService.findById(id);
            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/balances?year="
                            + balance.getAnnee()
                            + "&status=adjusted"
            );
        } catch (IllegalArgumentException exception) {
            SoldeConge balance = soldeCongeService.findById(id);
            if (balance == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("error", exception.getMessage());
            renderAdjustment(request, response, balance);
        }
    }

    private void renderAdjustment(
            HttpServletRequest request,
            HttpServletResponse response,
            SoldeConge balance
    ) throws ServletException, IOException {
        request.setAttribute("solde", balance);
        request.setAttribute(
                "formJoursAcquis",
                "POST".equalsIgnoreCase(request.getMethod())
                        ? request.getParameter("joursAcquis")
                        : valueOrZero(balance.getJoursAcquis())
        );
        request.setAttribute(
                "formJoursPris",
                "POST".equalsIgnoreCase(request.getMethod())
                        ? request.getParameter("joursPris")
                        : valueOrZero(balance.getJoursPris())
        );
        request.getRequestDispatcher(
                "/WEB-INF/views/admin/balances/form.jsp"
        ).forward(request, response);
    }

    private int parseYear(String value, HttpServletRequest request) {
        if (value == null || value.isBlank()) {
            return Year.now().getValue();
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "L'année doit être un nombre entier."
            );
        }
    }

    private Integer parseInteger(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Veuillez saisir un nombre entier valide."
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

    private String valueOrZero(Integer value) {
        return value == null ? "0" : value.toString();
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(
                value,
                java.nio.charset.StandardCharsets.UTF_8
        ).replace("+", "%20");
    }
}
