package com.gestconges.service;

import com.gestconges.dao.DemandeCongeDAO;
import com.gestconges.dao.EmployeDAO;
import com.gestconges.dao.SoldeCongeDAO;
import com.gestconges.dao.TypeCongeDAO;
import com.gestconges.model.DemandeConge;
import com.gestconges.model.Employe;
import com.gestconges.model.Role;
import com.gestconges.model.SoldeConge;
import com.gestconges.model.StatutDemande;
import com.gestconges.model.TypeConge;
import com.gestconges.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DemandeCongeService {

    private final DemandeCongeDAO demandeCongeDAO;
    private final EmployeDAO employeDAO;
    private final TypeCongeDAO typeCongeDAO;
    private final SoldeCongeDAO soldeCongeDAO;

    public DemandeCongeService() {
        this.demandeCongeDAO = new DemandeCongeDAO();
        this.employeDAO = new EmployeDAO();
        this.typeCongeDAO = new TypeCongeDAO();
        this.soldeCongeDAO = new SoldeCongeDAO();
    }

    public List<DemandeConge> findByEmployee(Long employeeId) {
        validateId(employeeId, "Identifiant employé invalide.");
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return demandeCongeDAO.findByEmployeeWithRelations(
                    session,
                    employeeId
            );
        }
    }

    public DemandeConge findByIdForEmployee(
            Long requestId,
            Long employeeId
    ) {
        validateId(requestId, "Identifiant de demande invalide.");
        validateId(employeeId, "Identifiant employé invalide.");
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            DemandeConge request =
                    demandeCongeDAO.findByIdWithRelations(session, requestId);
            if (request != null
                    && !request.getEmploye().getId().equals(employeeId)) {
                throw new SecurityException(
                        "Vous ne pouvez pas consulter cette demande."
                );
            }
            return request;
        }
    }

    public List<TypeConge> findActiveTypes() {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return typeCongeDAO.findActiveTypes(session);
        }
    }

    public List<DemandeConge> findPendingForManager(Long managerId) {
        validateId(managerId, "Identifiant du responsable invalide.");
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return demandeCongeDAO.findPendingForManager(
                    session,
                    managerId
            );
        }
    }

    public List<DemandeConge> findAllPending() {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return demandeCongeDAO.findAllPending(session);
        }
    }

    public void approveRequest(Long requestId, Long validatorId) {
        validateId(requestId, "Identifiant de demande invalide.");
        validateId(validatorId, "Identifiant du valideur invalide.");

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            Employe validator =
                    employeDAO.findById(session, validatorId);
            if (validator == null || !validator.isActif()) {
                throw new IllegalArgumentException(
                        "Valideur introuvable ou inactif."
                );
            }

            DemandeConge request =
                    demandeCongeDAO.findByIdWithRelations(
                            session,
                            requestId
                    );
            if (request == null) {
                throw new IllegalArgumentException(
                        "Demande de congé introuvable."
                );
            }

            validateDecisionScope(request, validator);
            ensurePending(request);

            if (request.getTypeConge().isDecompteSolde()) {
                deductBalance(session, request);
            }

            request.setStatut(StatutDemande.ACCEPTEE);
            request.setValideur(validator);
            request.setDateDecision(LocalDateTime.now());
            demandeCongeDAO.update(session, request);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void rejectRequest(
            Long requestId,
            Long validatorId,
            String comment
    ) {
        validateId(requestId, "Identifiant de demande invalide.");
        validateId(validatorId, "Identifiant du valideur invalide.");
        validateRejectionComment(comment);

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            Employe validator =
                    employeDAO.findById(session, validatorId);
            if (validator == null || !validator.isActif()) {
                throw new IllegalArgumentException(
                        "Valideur introuvable ou inactif."
                );
            }

            DemandeConge request =
                    demandeCongeDAO.findByIdWithRelations(
                            session,
                            requestId
                    );
            if (request == null) {
                throw new IllegalArgumentException(
                        "Demande de congé introuvable."
                );
            }

            validateDecisionScope(request, validator);
            ensurePending(request);

            request.setStatut(StatutDemande.REFUSEE);
            request.setValideur(validator);
            request.setDateDecision(LocalDateTime.now());
            request.setCommentaireDecision(comment.trim());
            demandeCongeDAO.update(session, request);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void createRequest(
            Long employeeId,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate,
            String motif
    ) {
        validateId(employeeId, "Identifiant employé invalide.");
        validateId(leaveTypeId, "Veuillez choisir un type de congé.");
        validateDates(startDate, endDate);
        validateMotif(motif);

        int workingDays = countWorkingDays(startDate, endDate);
        if (workingDays == 0) {
            throw new IllegalArgumentException(
                    "La période ne contient aucun jour ouvré."
            );
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            Employe employee = employeDAO.findById(session, employeeId);
            if (employee == null || !employee.isActif()) {
                throw new IllegalArgumentException(
                        "Employé introuvable ou inactif."
                );
            }

            TypeConge leaveType =
                    typeCongeDAO.findById(session, leaveTypeId);
            if (leaveType == null || !leaveType.isActif()) {
                throw new IllegalArgumentException(
                        "Type de congé introuvable ou inactif."
                );
            }

            if (demandeCongeDAO.hasOverlappingActiveRequest(
                    session,
                    employeeId,
                    startDate,
                    endDate,
                    null
            )) {
                throw new IllegalArgumentException(
                        "Vous avez déjà une demande active qui chevauche cette période."
                );
            }

            validateAvailableBalance(
                    session,
                    employeeId,
                    leaveType,
                    startDate,
                    workingDays
            );

            DemandeConge request = new DemandeConge(
                    employee,
                    leaveType,
                    startDate,
                    endDate,
                    workingDays,
                    normalizeMotif(motif)
            );
            demandeCongeDAO.save(session, request);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void updateRequest(
            Long requestId,
            Long employeeId,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate,
            String motif
    ) {
        validateId(requestId, "Identifiant de demande invalide.");
        validateId(employeeId, "Identifiant employé invalide.");
        validateId(leaveTypeId, "Veuillez choisir un type de congé.");
        validateDates(startDate, endDate);
        validateMotif(motif);

        int workingDays = countWorkingDays(startDate, endDate);
        if (workingDays == 0) {
            throw new IllegalArgumentException(
                    "La période ne contient aucun jour ouvré."
            );
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            DemandeConge request =
                    demandeCongeDAO.findById(session, requestId);
            if (request == null) {
                throw new IllegalArgumentException(
                        "Demande de congé introuvable."
                );
            }
            ensureOwnedBy(request, employeeId);
            ensurePending(request);

            TypeConge leaveType =
                    typeCongeDAO.findById(session, leaveTypeId);
            if (leaveType == null || !leaveType.isActif()) {
                throw new IllegalArgumentException(
                        "Type de congé introuvable ou inactif."
                );
            }

            if (demandeCongeDAO.hasOverlappingActiveRequest(
                    session,
                    employeeId,
                    startDate,
                    endDate,
                    requestId
            )) {
                throw new IllegalArgumentException(
                        "Vous avez déjà une demande active qui chevauche cette période."
                );
            }

            validateAvailableBalance(
                    session,
                    employeeId,
                    leaveType,
                    startDate,
                    workingDays
            );

            request.setTypeConge(leaveType);
            request.setDateDebut(startDate);
            request.setDateFin(endDate);
            request.setNbJours(workingDays);
            request.setMotif(normalizeMotif(motif));
            demandeCongeDAO.update(session, request);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void cancelRequest(Long requestId, Long employeeId) {
        validateId(requestId, "Identifiant de demande invalide.");
        validateId(employeeId, "Identifiant employé invalide.");

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            DemandeConge request =
                    demandeCongeDAO.findById(session, requestId);
            if (request == null) {
                throw new IllegalArgumentException(
                        "Demande de congé introuvable."
                );
            }
            ensureOwnedBy(request, employeeId);
            ensurePending(request);

            request.setStatut(StatutDemande.ANNULEE);
            demandeCongeDAO.update(session, request);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    static int countWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "Veuillez saisir une période valide."
            );
        }

        int count = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY
                    && day != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }

    static void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires."
            );
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "La date de fin doit être égale ou postérieure à la date de début."
            );
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Une demande ne peut pas commencer dans le passé."
            );
        }
    }

    static void validateMotif(String motif) {
        if (motif == null || motif.isBlank()) {
            throw new IllegalArgumentException(
                    "Le motif est obligatoire."
            );
        }
        if (motif.trim().length() > 1000) {
            throw new IllegalArgumentException(
                    "Le motif ne peut pas dépasser 1000 caractères."
            );
        }
    }

    private void validateAvailableBalance(
            Session session,
            Long employeeId,
            TypeConge leaveType,
            LocalDate startDate,
            int workingDays
    ) {
        if (!leaveType.isDecompteSolde()) {
            return;
        }

        SoldeConge balance =
                soldeCongeDAO.findByEmployeeTypeAndYear(
                        session,
                        employeeId,
                        leaveType.getId(),
                        startDate.getYear()
                );
        if (balance == null) {
            throw new IllegalArgumentException(
                    "Aucun solde annuel n'est initialisé pour ce type de congé."
            );
        }

        int acquired = balance.getJoursAcquis() == null
                ? 0
                : balance.getJoursAcquis();
        int taken = balance.getJoursPris() == null
                ? 0
                : balance.getJoursPris();
        int available = acquired - taken;
        if (workingDays > available) {
            throw new IllegalArgumentException(
                    "Le solde disponible est insuffisant pour cette demande."
            );
        }
    }

    static void validateRejectionComment(String comment) {
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException(
                    "Le commentaire de refus est obligatoire."
            );
        }
        if (comment.trim().length() > 1000) {
            throw new IllegalArgumentException(
                    "Le commentaire ne peut pas dépasser 1000 caractères."
            );
        }
    }

    static void validateDecisionScope(
            DemandeConge request,
            Employe validator
    ) {
        if (request.getEmploye().getId().equals(validator.getId())) {
            throw new SecurityException(
                    "Un employé ne peut pas décider de sa propre demande."
            );
        }

        if (validator.getRole() == Role.ADMIN) {
            return;
        }

        if (validator.getRole() != Role.RESPONSABLE
                || request.getEmploye().getManager() == null
                || !request.getEmploye().getManager().getId()
                        .equals(validator.getId())) {
            throw new SecurityException(
                    "Seul le responsable direct ou un administrateur "
                            + "peut décider cette demande."
            );
        }
    }

    private void deductBalance(
            Session session,
            DemandeConge request
    ) {
        SoldeConge balance =
                soldeCongeDAO.findByEmployeeTypeAndYear(
                        session,
                        request.getEmploye().getId(),
                        request.getTypeConge().getId(),
                        request.getDateDebut().getYear()
                );
        if (balance == null) {
            throw new IllegalArgumentException(
                    "Aucun solde annuel n'est initialisé pour cette demande."
            );
        }

        int acquired = balance.getJoursAcquis() == null
                ? 0
                : balance.getJoursAcquis();
        int taken = balance.getJoursPris() == null
                ? 0
                : balance.getJoursPris();
        int requestedDays = request.getNbJours() == null
                ? 0
                : request.getNbJours();

        validateApprovalCapacity(acquired, taken, requestedDays);

        balance.setJoursPris(taken + requestedDays);
        soldeCongeDAO.update(session, balance);
    }

    static void validateApprovalCapacity(
            int acquired,
            int taken,
            int requestedDays
    ) {
        if (requestedDays <= 0) {
            throw new IllegalArgumentException(
                    "Le nombre de jours de la demande est invalide."
            );
        }
        if (taken + requestedDays > acquired) {
            throw new IllegalArgumentException(
                    "Le solde disponible est insuffisant pour approuver "
                            + "cette demande."
            );
        }
    }

    private void ensureOwnedBy(DemandeConge request, Long employeeId) {
        if (!request.getEmploye().getId().equals(employeeId)) {
            throw new SecurityException(
                    "Vous ne pouvez pas modifier cette demande."
            );
        }
    }

    private void ensurePending(DemandeConge request) {
        if (request.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new IllegalArgumentException(
                    "Seules les demandes en attente peuvent être modifiées ou annulées."
            );
        }
    }

    private String normalizeMotif(String motif) {
        return motif == null ? null : motif.trim();
    }

    private void validateId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }
}
