package com.gestconges.dao;

import com.gestconges.model.Employe;
import com.gestconges.model.Role;
import org.hibernate.Session;

import java.util.List;

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

    public List<Employe> findAllWithDepartment(Session session) {
        return session.createQuery(
                        "FROM Employe e LEFT JOIN FETCH e.departement "
                                + "ORDER BY e.nom, e.prenom",
                        Employe.class
                )
                .getResultList();
    }

    public Employe findByIdWithDepartment(Session session, Long id) {
        return session.createQuery(
                        "FROM Employe e LEFT JOIN FETCH e.departement "
                                + "WHERE e.id = :id",
                        Employe.class
                )
                .setParameter("id", id)
                .uniqueResult();
    }

    public boolean emailExists(
            Session session,
            String email,
            Long excludedEmployeeId
    ) {
        String hql = "SELECT COUNT(e) FROM Employe e "
                + "WHERE LOWER(e.email) = LOWER(:email)";
        if (excludedEmployeeId != null) {
            hql += " AND e.id <> :excludedEmployeeId";
        }

        var query = session.createQuery(hql, Long.class)
                .setParameter("email", email);

        if (excludedEmployeeId != null) {
            query.setParameter("excludedEmployeeId", excludedEmployeeId);
        }

        return query.uniqueResult() > 0;
    }

    public boolean matriculeExists(
            Session session,
            String matricule,
            Long excludedEmployeeId
    ) {
        String hql = "SELECT COUNT(e) FROM Employe e "
                + "WHERE LOWER(e.matricule) = LOWER(:matricule)";
        if (excludedEmployeeId != null) {
            hql += " AND e.id <> :excludedEmployeeId";
        }

        var query = session.createQuery(hql, Long.class)
                .setParameter("matricule", matricule);

        if (excludedEmployeeId != null) {
            query.setParameter("excludedEmployeeId", excludedEmployeeId);
        }

        return query.uniqueResult() > 0;
    }

    public List<Employe> findByDepartmentWithManager(
            Session session,
            Long departmentId
    ) {
        return session.createQuery(
                        "FROM Employe e LEFT JOIN FETCH e.manager "
                                + "WHERE e.departement.id = :departmentId "
                                + "ORDER BY e.nom, e.prenom",
                        Employe.class
                )
                .setParameter("departmentId", departmentId)
                .getResultList();
    }

    public List<Employe> findActiveManagersByDepartment(
            Session session,
            Long departmentId
    ) {
        return session.createQuery(
                        "FROM Employe e "
                                + "WHERE e.departement.id = :departmentId "
                                + "AND e.role = :role AND e.actif = true "
                                + "ORDER BY e.nom, e.prenom",
                        Employe.class
                )
                .setParameter("departmentId", departmentId)
                .setParameter("role", Role.RESPONSABLE)
                .getResultList();
    }

    public List<Employe> findActiveEmployees(Session session) {
        return session.createQuery(
                        "FROM Employe employe "
                                + "WHERE employe.actif = true "
                                + "ORDER BY employe.nom, employe.prenom",
                        Employe.class
                )
                .getResultList();
    }
}