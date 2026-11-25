package com.gestconges.service;

import com.gestconges.model.DemandeConge;
import com.gestconges.model.Employe;
import com.gestconges.model.Role;
import com.gestconges.model.StatutDemande;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemandeCongeServiceTest {

    @Test
    void workingDaysExcludeSaturdayAndSunday() {
        LocalDate monday = LocalDate.of(2026, 11, 30);
        LocalDate sunday = LocalDate.of(2026, 12, 6);

        assertEquals(
                5,
                DemandeCongeService.countWorkingDays(monday, sunday)
        );
    }

    @Test
    void workingDaysRejectEndBeforeStart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DemandeCongeService.countWorkingDays(
                        LocalDate.of(2026, 12, 2),
                        LocalDate.of(2026, 12, 1)
                )
        );
    }

    @Test
    void dateValidationRejectsPastStartDate() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DemandeCongeService.validateDates(
                        LocalDate.now().minusDays(1),
                        LocalDate.now()
                )
        );
        assertTrue(exception.getMessage().contains("passé"));
    }

    @Test
    void dateValidationRejectsEndBeforeStart() {
        LocalDate future = LocalDate.now().plusDays(10);
        assertThrows(
                IllegalArgumentException.class,
                () -> DemandeCongeService.validateDates(
                        future.plusDays(2),
                        future
                )
        );
    }

    @Test
    void motifValidationRejectsBlankMotif() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DemandeCongeService.validateMotif("   ")
        );
    }

    @Test
    void motifValidationRejectsTextOverDatabaseLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DemandeCongeService.validateMotif("M".repeat(1001))
        );
    }

    @Test
    void ensureOwnedByRejectsDifferentEmployee() throws Exception {
        DemandeCongeService service = new DemandeCongeService();
        Employe employee = new Employe();
        employee.setId(2L);
        DemandeConge request = new DemandeConge();
        request.setEmploye(employee);

        Method ensureOwnedBy =
                DemandeCongeService.class.getDeclaredMethod(
                        "ensureOwnedBy",
                        DemandeConge.class,
                        Long.class
                );
        ensureOwnedBy.setAccessible(true);

        Exception exception = assertThrows(
                java.lang.reflect.InvocationTargetException.class,
                () -> ensureOwnedBy.invoke(service, request, 1L)
        );
        assertTrue(exception.getCause() instanceof SecurityException);
    }

    @Test
    void ensurePendingRejectsAcceptedRequest() throws Exception {
        DemandeCongeService service = new DemandeCongeService();
        DemandeConge request = new DemandeConge();
        request.setStatut(StatutDemande.ACCEPTEE);

        Method ensurePending =
                DemandeCongeService.class.getDeclaredMethod(
                        "ensurePending",
                        DemandeConge.class
                );
        ensurePending.setAccessible(true);

        Exception exception = assertThrows(
                java.lang.reflect.InvocationTargetException.class,
                () -> ensurePending.invoke(service, request)
        );
        assertTrue(
                exception.getCause() instanceof IllegalArgumentException
        );
    }

    @Test
    void rejectionRequiresComment() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DemandeCongeService.validateRejectionComment(" ")
        );
    }

    @Test
    void rejectionCommentCannotExceedDatabaseLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DemandeCongeService.validateRejectionComment(
                        "C".repeat(1001)
                )
        );
    }

    @Test
    void approvalRejectsInsufficientBalance() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DemandeCongeService.validateApprovalCapacity(5, 3, 3)
        );
    }

    @Test
    void approvalAllowsAvailableBalance() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> DemandeCongeService.validateApprovalCapacity(5, 3, 2)
        );
    }

    @Test
    void directManagerCanDecideDirectTeamRequest() {
        Employe manager = new Employe();
        manager.setId(10L);
        manager.setRole(Role.RESPONSABLE);

        Employe employee = new Employe();
        employee.setId(20L);
        employee.setManager(manager);

        DemandeConge request = new DemandeConge();
        request.setEmploye(employee);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> DemandeCongeService.validateDecisionScope(
                        request,
                        manager
                )
        );
    }

    @Test
    void employeeCannotDecideOwnRequest() {
        Employe employee = new Employe();
        employee.setId(20L);
        employee.setRole(Role.ADMIN);

        DemandeConge request = new DemandeConge();
        request.setEmploye(employee);

        assertThrows(
                SecurityException.class,
                () -> DemandeCongeService.validateDecisionScope(
                        request,
                        employee
                )
        );
    }

    @Test
    void administratorCanDecideAnyRequest() {
        Employe employee = new Employe();
        employee.setId(20L);

        Employe administrator = new Employe();
        administrator.setId(30L);
        administrator.setRole(Role.ADMIN);

        DemandeConge request = new DemandeConge();
        request.setEmploye(employee);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> DemandeCongeService.validateDecisionScope(
                        request,
                        administrator
                )
        );
    }
}
