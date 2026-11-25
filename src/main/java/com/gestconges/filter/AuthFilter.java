package com.gestconges.filter;

import com.gestconges.controller.DashboardPaths;
import com.gestconges.model.Role;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String requestPath = httpRequest.getRequestURI()
                .substring(contextPath.length());

        if (isPublicPath(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);

        if (session == null || session.getAttribute("employeId") == null) {
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        if (isAdminManagementPath(requestPath)
                && !Role.ADMIN.name().equals(
                session.getAttribute("employeRole"))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (isManagerApprovalPath(requestPath)
                && !Role.RESPONSABLE.name().equals(
                session.getAttribute("employeRole"))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (isManagerHistoryPath(requestPath)
                && !Role.RESPONSABLE.name().equals(
                session.getAttribute("employeRole"))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (isAdminHistoryPath(requestPath)
                && !Role.ADMIN.name().equals(
                session.getAttribute("employeRole"))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String requiredRole = DashboardPaths.roleForPath(requestPath);
        if (requiredRole != null
                && !requiredRole.equals(session.getAttribute("employeRole"))) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String requestPath) {
        return requestPath.equals("/")
                || requestPath.equals("/index.jsp")
                || requestPath.equals("/login")
                || requestPath.equals("/logout")
                || requestPath.equals("/favicon.ico")
                || requestPath.startsWith("/assets/")
                || requestPath.startsWith("/css/")
                || requestPath.startsWith("/images/")
                || requestPath.startsWith("/js/");
    }

    private boolean isAdminManagementPath(String requestPath) {
        return requestPath.equals("/admin/employees")
                || requestPath.startsWith("/admin/employees/")
                || requestPath.equals("/admin/departments")
                || requestPath.startsWith("/admin/departments/")
                || requestPath.equals("/admin/leave-types")
                || requestPath.startsWith("/admin/leave-types/")
                || requestPath.equals("/admin/balances")
                || requestPath.startsWith("/admin/balances/")
                || requestPath.equals("/admin/leave-requests")
                || requestPath.startsWith("/admin/leave-requests/");
    }

    private boolean isManagerApprovalPath(String requestPath) {
        return requestPath.equals("/manager/leave-requests")
                || requestPath.startsWith("/manager/leave-requests/");
    }

    private boolean isManagerHistoryPath(String requestPath) {
        return requestPath.equals("/manager/leave-history")
                || requestPath.startsWith("/manager/leave-history/");
    }

    private boolean isAdminHistoryPath(String requestPath) {
        return requestPath.equals("/admin/leave-history")
                || requestPath.startsWith("/admin/leave-history/");
    }
}
