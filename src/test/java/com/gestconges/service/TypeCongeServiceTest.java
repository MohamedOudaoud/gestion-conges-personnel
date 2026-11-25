package com.gestconges.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeCongeServiceTest {

    @Test
    void leaveTypeRequiresAnnualDaysWhenItDeductsBalance() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TypeCongeService.validateFields("Congé annuel", null, true)
        );

        assertTrue(exception.getMessage().contains("obligatoire"));
    }

    @Test
    void leaveTypeCanOmitAnnualDaysWhenItDoesNotDeductBalance() {
        assertDoesNotThrow(
                () -> TypeCongeService.validateFields(
                        "Congé exceptionnel",
                        null,
                        false
                )
        );
    }

    @Test
    void leaveTypeRejectsNegativeAnnualDays() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TypeCongeService.validateFields("Congé annuel", -1, true)
        );

        assertTrue(exception.getMessage().contains("négatif"));
    }

    @Test
    void leaveTypeRejectsBlankLabel() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TypeCongeService.validateFields("  ", null, false)
        );

        assertTrue(exception.getMessage().contains("libellé"));
    }

    @Test
    void leaveTypeRejectsLabelLongerThanDatabaseColumn() {
        String label = "C".repeat(256);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TypeCongeService.validateFields(label, null, false)
        );

        assertTrue(exception.getMessage().contains("255 caractères"));
    }
}
