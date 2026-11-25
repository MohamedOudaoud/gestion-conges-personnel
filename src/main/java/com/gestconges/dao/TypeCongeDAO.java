package com.gestconges.dao;

import com.gestconges.model.TypeConge;
import org.hibernate.Session;

import java.util.List;

public class TypeCongeDAO extends GenericDAO<TypeConge> {

    public TypeCongeDAO() {
        super(TypeConge.class);
    }

    public List<TypeConge> findAllOrderedByLibelle(Session session) {
        return session.createQuery(
                        "FROM TypeConge ORDER BY libelle",
                        TypeConge.class
                )
                .getResultList();
    }

    public boolean libelleExists(
            Session session,
            String libelle,
            Long excludedTypeCongeId
    ) {
        String hql = "SELECT COUNT(typeConge.id) FROM TypeConge typeConge "
                + "WHERE lower(typeConge.libelle) = lower(:libelle)";
        if (excludedTypeCongeId != null) {
            hql += " AND typeConge.id <> :excludedId";
        }

        var query = session.createQuery(hql, Long.class)
                .setParameter("libelle", libelle);
        if (excludedTypeCongeId != null) {
            query.setParameter("excludedId", excludedTypeCongeId);
        }
        return query.getSingleResult() > 0;
    }

    public List<TypeConge> findActiveBalanceDeducting(
            Session session
    ) {
        return session.createQuery(
                        "FROM TypeConge typeConge "
                                + "WHERE typeConge.actif = true "
                                + "AND typeConge.decompteSolde = true "
                                + "AND typeConge.joursParAn IS NOT NULL "
                                + "ORDER BY typeConge.libelle",
                        TypeConge.class
                )
                .getResultList();
    }
}