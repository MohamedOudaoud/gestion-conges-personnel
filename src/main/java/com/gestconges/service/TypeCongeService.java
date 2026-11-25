package com.gestconges.service;

import com.gestconges.dao.TypeCongeDAO;
import com.gestconges.model.TypeConge;
import com.gestconges.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class TypeCongeService {

    private final TypeCongeDAO typeCongeDAO;

    public TypeCongeService() {
        this.typeCongeDAO = new TypeCongeDAO();
    }

    public List<TypeConge> findAll() {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return typeCongeDAO.findAllOrderedByLibelle(session);
        }
    }

    public TypeConge findById(Long id) {
        validateId(id);
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            return typeCongeDAO.findById(session, id);
        }
    }

    public void createTypeConge(
            String libelle,
            Integer joursParAn,
            boolean decompteSolde,
            boolean justificatifRequis
    ) {
        validateFields(libelle, joursParAn, decompteSolde);
        String normalizedLibelle = libelle.trim();

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();
            ensureLibelleAvailable(session, normalizedLibelle, null);
            typeCongeDAO.save(
                    session,
                    new TypeConge(
                            normalizedLibelle,
                            joursParAn,
                            decompteSolde,
                            justificatifRequis
                    )
            );
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void updateTypeConge(
            Long id,
            String libelle,
            Integer joursParAn,
            boolean decompteSolde,
            boolean justificatifRequis
    ) {
        validateId(id);
        validateFields(libelle, joursParAn, decompteSolde);
        String normalizedLibelle = libelle.trim();

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();
            TypeConge typeConge = typeCongeDAO.findById(session, id);
            if (typeConge == null) {
                throw new IllegalArgumentException(
                        "Type de congé introuvable."
                );
            }

            ensureLibelleAvailable(session, normalizedLibelle, id);
            typeConge.setLibelle(normalizedLibelle);
            typeConge.setJoursParAn(joursParAn);
            typeConge.setDecompteSolde(decompteSolde);
            typeConge.setJustificatifRequis(justificatifRequis);
            typeCongeDAO.update(session, typeConge);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public void setActive(Long id, boolean active) {
        validateId(id);

        Transaction transaction = null;
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {
            transaction = session.beginTransaction();
            TypeConge typeConge = typeCongeDAO.findById(session, id);
            if (typeConge == null) {
                throw new IllegalArgumentException(
                        "Type de congé introuvable."
                );
            }

            typeConge.setActif(active);
            typeCongeDAO.update(session, typeConge);
            transaction.commit();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    static void validateFields(
            String libelle,
            Integer joursParAn,
            boolean decompteSolde
    ) {
        if (libelle == null || libelle.isBlank()) {
            throw new IllegalArgumentException(
                    "Le libellé du type de congé est obligatoire."
            );
        }
        if (libelle.trim().length() > 255) {
            throw new IllegalArgumentException(
                    "Le libellé ne peut pas dépasser 255 caractères."
            );
        }
        if (joursParAn != null && joursParAn < 0) {
            throw new IllegalArgumentException(
                    "Le nombre de jours par an ne peut pas être négatif."
            );
        }
        if (decompteSolde && joursParAn == null) {
            throw new IllegalArgumentException(
                    "Le nombre de jours par an est obligatoire lorsque le "
                            + "type décompte le solde."
            );
        }
    }

    private void ensureLibelleAvailable(
            Session session,
            String libelle,
            Long excludedTypeCongeId
    ) {
        if (typeCongeDAO.libelleExists(
                session,
                libelle,
                excludedTypeCongeId
        )) {
            throw new IllegalArgumentException(
                    "Un type de congé avec ce libellé existe déjà."
            );
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "L'identifiant du type de congé est invalide."
            );
        }
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }
}
