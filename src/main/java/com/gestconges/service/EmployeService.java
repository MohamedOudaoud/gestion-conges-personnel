package com.gestconges.service;

import com.gestconges.dao.DepartementDAO;
import com.gestconges.dao.EmployeDAO;
import com.gestconges.model.Departement;
import com.gestconges.model.Employe;
import com.gestconges.model.Role;
import com.gestconges.util.HibernateUtil;
import com.gestconges.util.PasswordUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class EmployeService {

    private final EmployeDAO employeDAO;
    private final DepartementDAO departementDAO;

    public EmployeService() {
        this.employeDAO = new EmployeDAO();
        this.departementDAO = new DepartementDAO();
    }

    public List<Employe> findAll() {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return employeDAO.findAllWithDepartment(session);
        }
    }

    public List<Departement> findAllDepartements() {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return departementDAO.findAllOrderedByName(session);
        }
    }

    public Employe findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Identifiant employé invalide.");
        }

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return employeDAO.findByIdWithDepartment(session, id);
        }
    }

    public void createEmployee(
            String matricule,
            String nom,
            String prenom,
            String email,
            String initialPassword,
            Role role,
            LocalDate dateEmbauche,
            Long departementId
    ) {
        String normalizedMatricule = requiredText(matricule, "matricule");
        String normalizedNom = requiredText(nom, "nom");
        String normalizedPrenom = requiredText(prenom, "prénom");
        String normalizedEmail = normalizeEmail(email);
        String password = requiredPassword(initialPassword);
        validateRoleAndDepartment(role, departementId);

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            ensureUnique(session, normalizedMatricule, normalizedEmail, null);
            Departement departement = findDepartment(session, departementId);

            Employe employe = new Employe(
                    normalizedMatricule,
                    normalizedNom,
                    normalizedPrenom,
                    normalizedEmail,
                    PasswordUtil.hashPassword(password),
                    role,
                    dateEmbauche
            );
            employe.setDepartement(departement);
            employeDAO.save(session, employe);

            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void updateEmployee(
            Long id,
            String matricule,
            String nom,
            String prenom,
            String email,
            Role role,
            LocalDate dateEmbauche,
            Long departementId
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Identifiant employé invalide.");
        }

        String normalizedMatricule = requiredText(matricule, "matricule");
        String normalizedNom = requiredText(nom, "nom");
        String normalizedPrenom = requiredText(prenom, "prénom");
        String normalizedEmail = normalizeEmail(email);
        validateRoleAndDepartment(role, departementId);

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            Employe employe = employeDAO.findByIdWithDepartment(session, id);
            if (employe == null) {
                throw new IllegalArgumentException("Employé introuvable.");
            }

            ensureUnique(
                    session,
                    normalizedMatricule,
                    normalizedEmail,
                    id
            );
            employe.setMatricule(normalizedMatricule);
            employe.setNom(normalizedNom);
            employe.setPrenom(normalizedPrenom);
            employe.setEmail(normalizedEmail);
            employe.setRole(role);
            employe.setDateEmbauche(dateEmbauche);
            employe.setDepartement(findDepartment(session, departementId));
            employeDAO.update(session, employe);

            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void setActive(Long id, boolean active) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Identifiant employé invalide.");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            Employe employe = employeDAO.findById(session, id);
            if (employe == null) {
                throw new IllegalArgumentException("Employé introuvable.");
            }

            employe.setActif(active);
            employeDAO.update(session, employe);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    private void ensureUnique(
            Session session,
            String matricule,
            String email,
            Long excludedEmployeeId
    ) {
        if (employeDAO.matriculeExists(
                session,
                matricule,
                excludedEmployeeId
        )) {
            throw new IllegalArgumentException(
                    "Ce matricule est déjà utilisé."
            );
        }

        if (employeDAO.emailExists(session, email, excludedEmployeeId)) {
            throw new IllegalArgumentException(
                    "Cette adresse email est déjà utilisée."
            );
        }
    }

    private Departement findDepartment(Session session, Long departementId) {
        Departement departement = departementDAO.findById(
                session,
                departementId
        );
        if (departement == null) {
            throw new IllegalArgumentException("Département introuvable.");
        }
        return departement;
    }

    private void validateRoleAndDepartment(Role role, Long departementId) {
        if (role == null) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }
        if (departementId == null || departementId <= 0) {
            throw new IllegalArgumentException(
                    "Le département est obligatoire."
            );
        }
    }

    private String normalizeEmail(String email) {
        String normalizedEmail = requiredText(email, "email")
                .toLowerCase(Locale.ROOT);
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Veuillez saisir une adresse email valide."
            );
        }
        return normalizedEmail;
    }

    private String requiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Le champ " + fieldName + " est obligatoire."
            );
        }
        return value.trim();
    }

    private String requiredPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Le champ mot de passe est obligatoire."
            );
        }
        return password;
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }
}
