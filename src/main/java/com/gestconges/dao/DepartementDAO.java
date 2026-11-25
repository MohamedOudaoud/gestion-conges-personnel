package com.gestconges.dao;

import com.gestconges.model.Departement;
import org.hibernate.Session;

import java.util.List;

public class DepartementDAO extends GenericDAO<Departement> {

    public DepartementDAO() {
        super(Departement.class);
    }

    public List<Departement> findAllOrderedByName(Session session) {
        return session.createQuery(
                        "FROM Departement ORDER BY nom",
                        Departement.class
                )
                .getResultList();
    }
}