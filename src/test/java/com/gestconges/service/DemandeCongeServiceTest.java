package com.gestconges.service;

import com.gestconges.model.DemandeConge;
import com.gestconges.model.Employe;
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
}
