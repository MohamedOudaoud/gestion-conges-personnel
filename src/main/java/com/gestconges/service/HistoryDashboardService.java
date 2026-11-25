package com.gestconges.service;

import com.gestconges.dao.DemandeCongeDAO;
import com.gestconges.dao.EmployeDAO;
import com.gestconges.dao.SoldeCongeDAO;
import com.gestconges.dao.TypeCongeDAO;
import com.gestconges.model.DemandeConge;
import com.gestconges.model.Role;
import com.gestconges.model.StatutDemande;
import com.gestconges.model.TypeConge;
import com.gestconges.util.HibernateUtil;
import org.hibernate.Session;

import java.time.LocalDate;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HistoryDashboardService {

    private final DemandeCongeDAO demandeCongeDAO;
    private final EmployeDAO employeDAO;
    private final TypeCongeDAO typeCongeDAO;

    public HistoryDashboardService() {
        this.demandeCongeDAO = new DemandeCongeDAO();
        this.employeDAO = new EmployeDAO();
        this.typeCongeDAO = new TypeCongeDAO();
    }

    public List<DemandeConge> findPersonalHistory(
            Long employeeId,
            String statusValue,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateId(employeeId, "Identifiant employé invalide.");
        validateFilterDates(startDate, endDate);
        StatutDemande status = parseStatus(statusValue);

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return demandeCongeDAO.findHistory(
                    session,
                    employeeId,
                    null,
                    false,
                    status,
                    leaveTypeId,
                    startDate,
                    endDate
            );
        }
    }

    public List<DemandeConge> findTeamHistory(
            Long managerId,
            String statusValue,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateId(managerId, "Identifiant responsable invalide.");
        validateFilterDates(startDate, endDate);
        StatutDemande status = parseStatus(statusValue);

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return demandeCongeDAO.findHistory(
                    session,
                    null,
                    managerId,
                    false,
                    status,
                    leaveTypeId,
                    startDate,
                    endDate
            );
        }
    }

    public List<DemandeConge> findAllHistory(
            String statusValue,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateFilterDates(startDate, endDate);
        StatutDemande status = parseStatus(statusValue);

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return demandeCongeDAO.findHistory(
                    session,
                    null,
                    null,
                    true,
                    status,
                    leaveTypeId,
                    startDate,
                    endDate
            );
        }
    }

    public Map<String, Long> getPersonalSummary(Long employeeId) {
        validateId(employeeId, "Identifiant employé invalide.");
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            Map<String, Long> summary = new LinkedHashMap<>();
            summary.put(
                    "pending",
                    demandeCongeDAO.countByStatus(
                            session,
                            StatutDemande.EN_ATTENTE,
                            employeeId,
                            null,
                            false
                    )
            );
            summary.put(
                    "accepted",
                    demandeCongeDAO.countByStatus(
                            session,
                            StatutDemande.ACCEPTEE,
                            employeeId,
                            null,
                            false
                    )
            );
            summary.put(
                    "rejected",
                    demandeCongeDAO.countByStatus(
                            session,
                            StatutDemande.REFUSEE,
                            employeeId,
                            null,
                            false
                    )
            );
            return summary;
        }
    }

    public Map<String, Long> getTeamSummary(Long managerId) {
        validateId(managerId, "Identifiant responsable invalide.");
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            Map<String, Long> summary = new LinkedHashMap<>();
            summary.put(
                    "pending",
                    demandeCongeDAO.countByStatus(
                            session,
                            StatutDemande.EN_ATTENTE,
                            null,
                            managerId,
                            false
                    )
            );
            summary.put(
                    "accepted",
                    demandeCongeDAO.countByStatus(
                            session,
                            StatutDemande.ACCEPTEE,
                            null,
                            managerId,
                            false
                    )
            );
            summary.put(
                    "rejected",
                    demandeCongeDAO.countByStatus(
                            session,
                            StatutDemande.REFUSEE,
                            null,
                            managerId,
                            false
                    )
            );
            return summary;
        }
    }

    public Map<String, Long> getAdminSummary() {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            Map<String, Long> summary = new LinkedHashMap<>();
            summary.put(
                    "activeEmployees",
                    employeDAO.countActive(session)
            );
            summary.put(
                    "activeLeaveTypes",
                    typeCongeDAO.countActive(session)
            );
            summary.put(
                    "pending",
                    demandeCongeDAO.countByStatus(
                            session,
                            StatutDemande.EN_ATTENTE,
                            null,
                            null,
                            true
                    )
            );
            summary.put(
                    "accepted",
                    demandeCongeDAO.countByStatus(
                            session,
                            StatutDemande.ACCEPTEE,
                            null,
                            null,
                            true
                    )
            );
            summary.put(
                    "rejected",
                    demandeCongeDAO.countByStatus(
                            session,
                            StatutDemande.REFUSEE,
                            null,
                            null,
                            true
                    )
            );
            return summary;
        }
    }

    public List<TypeConge> findLeaveTypes() {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return typeCongeDAO.findAllOrderedByLibelle(session);
        }
    }

    public static StatutDemande parseStatus(String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return null;
        }
        try {
            return StatutDemande.valueOf(statusValue);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Le statut sélectionné est invalide."
            );
        }
    }

    public static void validateFilterDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "La date de fin doit être égale ou postérieure à la date de début."
            );
        }
    }

    private void validateId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
