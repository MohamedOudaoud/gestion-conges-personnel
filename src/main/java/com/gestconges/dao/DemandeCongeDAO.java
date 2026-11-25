package com.gestconges.dao;

import com.gestconges.model.DemandeConge;
import com.gestconges.model.StatutDemande;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public class DemandeCongeDAO extends GenericDAO<DemandeConge> {

    public DemandeCongeDAO() {
        super(DemandeConge.class);
    }

    public List<DemandeConge> findByEmployeeWithRelations(
            Session session,
            Long employeeId
    ) {
        return session.createQuery(
                        "FROM DemandeConge demande "
                                + "JOIN FETCH demande.employe employe "
                                + "LEFT JOIN FETCH employe.manager "
                                + "JOIN FETCH demande.typeConge typeConge "
                                + "WHERE employe.id = :employeeId "
                                + "ORDER BY demande.dateDemande DESC",
                        DemandeConge.class
                )
                .setParameter("employeeId", employeeId)
                .getResultList();
    }

    public DemandeConge findByIdWithRelations(
            Session session,
            Long id
    ) {
        return session.createQuery(
                        "FROM DemandeConge demande "
                                + "JOIN FETCH demande.employe employe "
                                + "LEFT JOIN FETCH employe.manager "
                                + "JOIN FETCH demande.typeConge typeConge "
                                + "WHERE demande.id = :id",
                        DemandeConge.class
                )
                .setParameter("id", id)
                .uniqueResult();
    }

    public boolean hasOverlappingActiveRequest(
            Session session,
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            Long excludedRequestId
    ) {
        String hql = "SELECT COUNT(demande.id) FROM DemandeConge demande "
                + "WHERE demande.employe.id = :employeeId "
                + "AND demande.statut IN (:pending, :accepted) "
                + "AND demande.dateDebut <= :endDate "
                + "AND demande.dateFin >= :startDate";
        if (excludedRequestId != null) {
            hql += " AND demande.id <> :excludedRequestId";
        }

        var query = session.createQuery(hql, Long.class)
                .setParameter("employeeId", employeeId)
                .setParameter("pending", StatutDemande.EN_ATTENTE)
                .setParameter("accepted", StatutDemande.ACCEPTEE)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);
        if (excludedRequestId != null) {
            query.setParameter("excludedRequestId", excludedRequestId);
        }
        return query.getSingleResult() > 0;
    }

    public List<DemandeConge> findPendingForManager(
            Session session,
            Long managerId
    ) {
        return session.createQuery(
                        "FROM DemandeConge demande "
                                + "JOIN FETCH demande.employe employe "
                                + "JOIN FETCH demande.typeConge typeConge "
                                + "WHERE demande.statut = :pending "
                                + "AND employe.manager.id = :managerId "
                                + "ORDER BY demande.dateDemande",
                        DemandeConge.class
                )
                .setParameter("pending", StatutDemande.EN_ATTENTE)
                .setParameter("managerId", managerId)
                .getResultList();
    }

    public List<DemandeConge> findAllPending(
            Session session
    ) {
        return session.createQuery(
                        "FROM DemandeConge demande "
                                + "JOIN FETCH demande.employe employe "
                                + "JOIN FETCH demande.typeConge typeConge "
                                + "WHERE demande.statut = :pending "
                                + "ORDER BY demande.dateDemande",
                        DemandeConge.class
                )
                .setParameter("pending", StatutDemande.EN_ATTENTE)
                .getResultList();
    }

    public List<DemandeConge> findHistory(
            Session session,
            Long employeeId,
            Long managerId,
            boolean allEmployees,
            StatutDemande status,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        StringBuilder hql = new StringBuilder(
                "FROM DemandeConge demande "
                        + "JOIN FETCH demande.employe employe "
                        + "LEFT JOIN FETCH employe.manager "
                        + "JOIN FETCH demande.typeConge typeConge "
                        + "WHERE 1 = 1"
        );

        if (!allEmployees) {
            if (employeeId != null) {
                hql.append(" AND employe.id = :employeeId");
            } else if (managerId != null) {
                hql.append(" AND employe.manager.id = :managerId");
            }
        }
        if (status != null) {
            hql.append(" AND demande.statut = :status");
        }
        if (leaveTypeId != null) {
            hql.append(" AND typeConge.id = :leaveTypeId");
        }
        if (startDate != null) {
            hql.append(" AND demande.dateDebut >= :startDate");
        }
        if (endDate != null) {
            hql.append(" AND demande.dateFin <= :endDate");
        }
        hql.append(" ORDER BY demande.dateDemande DESC");

        var query = session.createQuery(hql.toString(), DemandeConge.class);
        if (!allEmployees) {
            if (employeeId != null) {
                query.setParameter("employeeId", employeeId);
            } else if (managerId != null) {
                query.setParameter("managerId", managerId);
            }
        }
        if (status != null) {
            query.setParameter("status", status);
        }
        if (leaveTypeId != null) {
            query.setParameter("leaveTypeId", leaveTypeId);
        }
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        return query.getResultList();
    }

    public long countByStatus(
            Session session,
            StatutDemande status,
            Long employeeId,
            Long managerId,
            boolean allEmployees
    ) {
        String hql = "SELECT COUNT(demande.id) FROM DemandeConge demande "
                + "WHERE demande.statut = :status";
        if (!allEmployees) {
            if (employeeId != null) {
                hql += " AND demande.employe.id = :employeeId";
            } else if (managerId != null) {
                hql += " AND demande.employe.manager.id = :managerId";
            }
        }

        var query = session.createQuery(hql, Long.class)
                .setParameter("status", status);
        if (!allEmployees) {
            if (employeeId != null) {
                query.setParameter("employeeId", employeeId);
            } else if (managerId != null) {
                query.setParameter("managerId", managerId);
            }
        }
        return query.getSingleResult();
    }
}