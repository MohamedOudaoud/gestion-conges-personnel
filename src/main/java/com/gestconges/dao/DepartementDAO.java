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

    public boolean nameExists(
            Session session,
            String name,
            Long excludedDepartmentId
    ) {
        String hql = "SELECT COUNT(d) FROM Departement d "
                + "WHERE LOWER(d.nom) = LOWER(:name)";
        if (excludedDepartmentId != null) {
            hql += " AND d.id <> :excludedDepartmentId";
        }

        var query = session.createQuery(hql, Long.class)
                .setParameter("name", name);
        if (excludedDepartmentId != null) {
            query.setParameter("excludedDepartmentId", excludedDepartmentId);
        }
        return query.uniqueResult() > 0;
    }

    public long countEmployees(Session session, Long departmentId) {
        return session.createQuery(
                        "SELECT COUNT(e) FROM Employe e "
                                + "WHERE e.departement.id = :departmentId",
                        Long.class
                )
                .setParameter("departmentId", departmentId)
                .uniqueResult();
    }
}