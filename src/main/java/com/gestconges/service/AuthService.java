package com.gestconges.service;

import com.gestconges.dao.EmployeDAO;
import com.gestconges.model.Employe;
import com.gestconges.util.HibernateUtil;
import com.gestconges.util.PasswordUtil;

import org.hibernate.Session;

public class AuthService {

    private final EmployeDAO employeDAO;

    public AuthService() {
        this.employeDAO = new EmployeDAO();
    }

    public Employe authenticate(String email, String password) {

        // Validate input
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            // Find employee by email
            Employe employe = employeDAO.findByEmail(
                    session,
                    email
            );

            // Account doesn't exist or is inactive
            if (employe == null || !employe.isActif()) {
                return null;
            }

            // Verify password
            boolean passwordCorrect = PasswordUtil.verifyPassword(
                    password,
                    employe.getMotDePasse()
            );

            if (!passwordCorrect) {
                return null;
            }

            return employe;
        }
    }
}