package com.gestconges.service;

import com.gestconges.dao.EmployeDAO;
import com.gestconges.dao.SoldeCongeDAO;
import com.gestconges.dao.TypeCongeDAO;
import com.gestconges.model.Employe;
import com.gestconges.model.SoldeConge;
import com.gestconges.model.TypeConge;
import com.gestconges.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.Year;
import java.util.List;

public class SoldeCongeService {

    private final SoldeCongeDAO soldeCongeDAO;
    private final EmployeDAO employeDAO;
    private final TypeCongeDAO typeCongeDAO;

    public SoldeCongeService() {
        this.soldeCongeDAO = new SoldeCongeDAO();
        this.employeDAO = new EmployeDAO();
        this.typeCongeDAO = new TypeCongeDAO();
    }

    public List<SoldeConge> findByYear(int year) {
        validateYear(year);
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return soldeCongeDAO.findByYearWithRelations(session, year);
        }
    }

    public SoldeConge findById(Long id) {
        validateId(id, "Identifiant du solde invalide.");
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return soldeCongeDAO.findByIdWithRelations(session, id);
        }
    }

    public int initializeYear(int year) {
        validateYear(year);

        Transaction transaction = null;
        int initializedCount = 0;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            List<Employe> employees = employeDAO.findActiveEmployees(session);
            List<TypeConge> leaveTypes =
                    typeCongeDAO.findActiveBalanceDeducting(session);

            for (Employe employee : employees) {
                for (TypeConge leaveType : leaveTypes) {
                    SoldeConge existing =
                            soldeCongeDAO.findByEmployeeTypeAndYear(
                                    session,
                                    employee.getId(),
                                    leaveType.getId(),
                                    year
                            );
                    if (existing == null) {
                        soldeCongeDAO.save(
                                session,
                                new SoldeConge(
                                        employee,
                                        leaveType,
                                        year,
                                        leaveType.getJoursParAn(),
                                        0
                                )
                        );
                        initializedCount++;
                    }
                }
            }

            transaction.commit();
            return initializedCount;
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void adjustBalance(
            Long balanceId,
            Integer acquiredDays,
            Integer takenDays
    ) {
        validateId(balanceId, "Identifiant du solde invalide.");
        validateAdjustment(acquiredDays, takenDays);

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();

            SoldeConge balance =
                    soldeCongeDAO.findById(session, balanceId);
            if (balance == null) {
                throw new IllegalArgumentException(
                        "Solde de congé introuvable."
                );
            }

            balance.setJoursAcquis(acquiredDays);
            balance.setJoursPris(takenDays);
            soldeCongeDAO.update(session, balance);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    static void validateAdjustment(
            Integer acquiredDays,
            Integer takenDays
    ) {
        if (acquiredDays == null || takenDays == null) {
            throw new IllegalArgumentException(
                    "Les jours acquis et les jours pris sont obligatoires."
            );
        }
        if (acquiredDays < 0 || takenDays < 0) {
            throw new IllegalArgumentException(
                    "Les nombres de jours ne peuvent pas être négatifs."
            );
        }
        if (takenDays > acquiredDays) {
            throw new IllegalArgumentException(
                    "Les jours pris ne peuvent pas dépasser les jours acquis."
            );
        }
    }

    static void validateYear(int year) {
        int currentYear = Year.now().getValue();
        if (year < 2000 || year > currentYear + 10) {
            throw new IllegalArgumentException(
                    "Veuillez saisir une année valide."
            );
        }
    }

    private void validateId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }
}
