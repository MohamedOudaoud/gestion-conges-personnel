package com.gestconges.dao;

import com.gestconges.model.Departement;
import com.gestconges.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class GenericDAOTest {

    public static void main(String[] args) {

        DepartementDAO departementDAO = new DepartementDAO();

        Transaction transaction = null;

        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            transaction = session.beginTransaction();

            // 1. Insert a department
            Departement departement = new Departement();
            departement.setNom("Ressources Humaines");
            departement.setDescription("Service des ressources humaines");

            departementDAO.save(session, departement);

            System.out.println("Department inserted successfully!");

            // 2. Retrieve the inserted department
            Departement found = departementDAO.findById(
                    session,
                    departement.getId()
            );

            System.out.println("Found department: " + found.getNom());

            // 3. Retrieve all departments
            List<Departement> departements =
                    departementDAO.findAll(session);

            System.out.println("\nList of departments:");

            for (Departement d : departements) {
                System.out.println(d.getId() + " - " + d.getNom());
            }

            // 4. Update the department
            found.setDescription("Updated description");

            departementDAO.update(session, found);

            System.out.println("\nDepartment updated successfully!");

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            e.printStackTrace();

        } finally {
            HibernateUtil.shutdown();
        }
    }
}