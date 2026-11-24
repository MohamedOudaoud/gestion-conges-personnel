package com.gestconges.dao;

import com.gestconges.model.Employe;
import org.hibernate.Session;

public class EmployeDAO extends GenericDAO<Employe> {

    public EmployeDAO() {
        super(Employe.class);
    }

    public Employe findByEmail(Session session, String email) {

        return session.createQuery(
                        "FROM Employe e WHERE LOWER(e.email) = LOWER(:email)",
                        Employe.class
                )
                .setParameter("email", email.trim())
                .uniqueResult();
    }

}