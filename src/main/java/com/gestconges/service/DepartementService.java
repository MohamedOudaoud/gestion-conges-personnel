package com.gestconges.service;

import com.gestconges.dao.DepartementDAO;
import com.gestconges.dao.EmployeDAO;
import com.gestconges.model.Departement;
import com.gestconges.model.Employe;
import com.gestconges.model.Role;
import com.gestconges.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DepartementService {

    private final DepartementDAO departementDAO;
    private final EmployeDAO employeDAO;

    public DepartementService() {
        this.departementDAO = new DepartementDAO();
        this.employeDAO = new EmployeDAO();
    }

    public List<Departement> findAll() {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return departementDAO.findAllOrderedByName(session);
        }
    }

    public Departement findById(Long id) {
        validateId(id, "département");
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return departementDAO.findById(session, id);
        }
    }

    public void createDepartment(String name, String description) {
        String normalizedName = validateName(name);
        String normalizedDescription = normalizeDescription(description);

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();
            ensureNameAvailable(session, normalizedName, null);

            departementDAO.save(
                    session,
                    new Departement(normalizedName, normalizedDescription)
            );
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void updateDepartment(
            Long id,
            String name,
            String description
    ) {
        validateId(id, "département");
        String normalizedName = validateName(name);
        String normalizedDescription = normalizeDescription(description);

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();
            Departement departement = departementDAO.findById(session, id);
            if (departement == null) {
                throw new IllegalArgumentException("Département introuvable.");
            }

            ensureNameAvailable(session, normalizedName, id);
            departement.setNom(normalizedName);
            departement.setDescription(normalizedDescription);
            departementDAO.update(session, departement);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void deleteDepartment(Long id) {
        validateId(id, "département");

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();
            Departement departement = departementDAO.findById(session, id);
            if (departement == null) {
                throw new IllegalArgumentException("Département introuvable.");
            }

            if (departementDAO.countEmployees(session, id) > 0) {
                throw new IllegalArgumentException(
                        "Impossible de supprimer un département qui contient "
                                + "encore des employés."
                );
            }

            departementDAO.delete(session, departement);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public List<Employe> findEmployees(Long departmentId) {
        validateId(departmentId, "département");
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            requireDepartment(session, departmentId);
            return employeDAO.findByDepartmentWithManager(
                    session,
                    departmentId
            );
        }
    }

    public List<Employe> findManagerCandidates(Long departmentId) {
        validateId(departmentId, "département");
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            requireDepartment(session, departmentId);
            return employeDAO.findActiveManagersByDepartment(
                    session,
                    departmentId
            );
        }
    }

    public void assignManager(
            Long departmentId,
            Long managerId,
            List<Long> employeeIds
    ) {
        validateId(departmentId, "département");
        validateId(managerId, "responsable");
        Set<Long> selectedIds = validateEmployeeIds(employeeIds);

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();
            requireDepartment(session, departmentId);
            Employe manager = employeDAO.findById(session, managerId);
            if (manager == null || !manager.isActif()
                    || manager.getRole() != Role.RESPONSABLE
                    || manager.getDepartement() == null
                    || !departmentId.equals(
                    manager.getDepartement().getId())) {
                throw new IllegalArgumentException(
                        "Le responsable doit être actif et appartenir au "
                                + "département sélectionné."
                );
            }

            for (Long employeeId : selectedIds) {
                Employe employee = employeDAO.findById(session, employeeId);
                validateTeamMember(employee, employeeId, departmentId, manager);
                ensureNoManagerCycle(employee, manager);
                employee.setManager(manager);
            }
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void unassignManager(Long departmentId, List<Long> employeeIds) {
        validateId(departmentId, "département");
        Set<Long> selectedIds = validateEmployeeIds(employeeIds);

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();
            requireDepartment(session, departmentId);

            for (Long employeeId : selectedIds) {
                Employe employee = employeDAO.findById(session, employeeId);
                if (employee == null || employee.getDepartement() == null
                        || !departmentId.equals(
                        employee.getDepartement().getId())) {
                    throw new IllegalArgumentException(
                            "Un employé sélectionné n'appartient pas à ce "
                                    + "département."
                    );
                }
                employee.setManager(null);
            }
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    private void validateTeamMember(
            Employe employee,
            Long employeeId,
            Long departmentId,
            Employe manager
    ) {
        if (employee == null || employee.getDepartement() == null
                || !departmentId.equals(employee.getDepartement().getId())) {
            throw new IllegalArgumentException(
                    "Un employé sélectionné n'appartient pas à ce département."
            );
        }
        if (employeeId.equals(manager.getId())) {
            throw new IllegalArgumentException(
                    "Un responsable ne peut pas être son propre subordonné."
            );
        }
        if (employee.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Un administrateur ne peut pas être affecté à une équipe."
            );
        }
    }

    private void ensureNoManagerCycle(Employe employee, Employe manager) {
        Set<Long> visitedIds = new HashSet<>();
        Employe ancestor = manager;
        while (ancestor != null) {
            Long ancestorId = ancestor.getId();
            if (employee.getId().equals(ancestorId)) {
                throw new IllegalArgumentException(
                        "Cette affectation créerait un cycle hiérarchique."
                );
            }
            if (!visitedIds.add(ancestorId)) {
                throw new IllegalArgumentException(
                        "La hiérarchie actuelle contient déjà un cycle."
                );
            }
            ancestor = ancestor.getManager();
        }
    }

    private Departement requireDepartment(Session session, Long id) {
        Departement department = departementDAO.findById(session, id);
        if (department == null) {
            throw new IllegalArgumentException("Département introuvable.");
        }
        return department;
    }

    private Set<Long> validateEmployeeIds(List<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sélectionnez au moins un employé."
            );
        }
        Set<Long> selectedIds = new HashSet<>();
        for (Long employeeId : employeeIds) {
            validateId(employeeId, "employé");
            selectedIds.add(employeeId);
        }
        return selectedIds;
    }

    private void ensureNameAvailable(
            Session session,
            String name,
            Long excludedDepartmentId
    ) {
        if (departementDAO.nameExists(session, name, excludedDepartmentId)) {
            throw new IllegalArgumentException(
                    "Un département avec ce nom existe déjà."
            );
        }
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du département est obligatoire."
            );
        }
        String normalizedName = name.trim();
        if (normalizedName.length() > 255) {
            throw new IllegalArgumentException(
                    "Le nom du département ne peut pas dépasser 255 caractères."
            );
        }
        return normalizedName;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String normalizedDescription = description.trim();
        if (normalizedDescription.length() > 255) {
            throw new IllegalArgumentException(
                    "La description ne peut pas dépasser 255 caractères."
            );
        }
        return normalizedDescription;
    }

    private void validateId(Long id, String label) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "L'identifiant " + label + " est invalide."
            );
        }
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }
}
