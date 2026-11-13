package com.gestconges.util;

import com.gestconges.model.Departement;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateTest {

    public static void main(String[] args) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            Departement departement = new Departement();
            departement.setNom("Informatique");
            departement.setDescription("Service informatique");

            session.persist(departement);

            transaction.commit();

            System.out.println("Department inserted successfully!");

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