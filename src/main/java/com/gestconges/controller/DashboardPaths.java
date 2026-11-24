package com.gestconges.controller;

import com.gestconges.model.Role;

public final class DashboardPaths {

    private DashboardPaths() {
    }

    public static String pathForRole(String role) {
        if (role == null) {
            return null;
        }

        return switch (role) {
            case "EMPLOYE" -> "/dashboard/employe";
            case "RESPONSABLE" -> "/dashboard/responsable";
            case "ADMIN" -> "/dashboard/admin";
            default -> null;
        };
    }

    public static String roleForPath(String path) {
        if (path == null) {
            return null;
        }

        if (path.equals("/dashboard/employe")) {
            return Role.EMPLOYE.name();
        }
        if (path.equals("/dashboard/responsable")) {
            return Role.RESPONSABLE.name();
        }
        if (path.equals("/dashboard/admin")) {
            return Role.ADMIN.name();
        }
        return null;
    }
}
