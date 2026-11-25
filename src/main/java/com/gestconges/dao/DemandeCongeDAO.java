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
}