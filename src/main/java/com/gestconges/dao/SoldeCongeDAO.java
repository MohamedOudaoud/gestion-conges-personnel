package com.gestconges.dao;

import com.gestconges.model.SoldeConge;
import org.hibernate.Session;

import java.util.List;

public class SoldeCongeDAO extends GenericDAO<SoldeConge> {

    public SoldeCongeDAO() {
        super(SoldeConge.class);
    }

    public List<SoldeConge> findByYearWithRelations(
            Session session,
            int year
    ) {
        return session.createQuery(
                        "FROM SoldeConge solde "
                                + "JOIN FETCH solde.employe employe "
                                + "JOIN FETCH solde.typeConge typeConge "
                                + "WHERE solde.annee = :year "
                                + "ORDER BY employe.nom, employe.prenom, "
                                + "typeConge.libelle",
                        SoldeConge.class
                )
                .setParameter("year", year)
                .getResultList();
    }

    public SoldeConge findByIdWithRelations(Session session, Long id) {
        return session.createQuery(
                        "FROM SoldeConge solde "
                                + "JOIN FETCH solde.employe employe "
                                + "JOIN FETCH solde.typeConge typeConge "
                                + "WHERE solde.id = :id",
                        SoldeConge.class
                )
                .setParameter("id", id)
                .uniqueResult();
    }

    public SoldeConge findByEmployeeTypeAndYear(
            Session session,
            Long employeeId,
            Long leaveTypeId,
            int year
    ) {
        return session.createQuery(
                        "FROM SoldeConge solde "
                                + "WHERE solde.employe.id = :employeeId "
                                + "AND solde.typeConge.id = :leaveTypeId "
                                + "AND solde.annee = :year",
                        SoldeConge.class
                )
                .setParameter("employeeId", employeeId)
                .setParameter("leaveTypeId", leaveTypeId)
                .setParameter("year", year)
                .uniqueResult();
    }
}