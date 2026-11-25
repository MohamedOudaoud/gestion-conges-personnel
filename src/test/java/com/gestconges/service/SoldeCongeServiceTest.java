package com.gestconges.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoldeCongeServiceTest {

    @Test
    void adjustmentRejectsNegativeAcquiredDays() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SoldeCongeService.validateAdjustment(-1, 0)
        );
        assertTrue(exception.getMessage().contains("négatifs"));
    }

    @Test
    void adjustmentRejectsNegativeTakenDays() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SoldeCongeService.validateAdjustment(20, -1)
        );
        assertTrue(exception.getMessage().contains("négatifs"));
    }

    @Test
    void adjustmentRejectsTakenDaysAboveAcquiredDays() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SoldeCongeService.validateAdjustment(10, 11)
        );
        assertTrue(exception.getMessage().contains("ne peuvent pas dépasser"));
    }

    @Test
    void adjustmentAllowsValidValues() {
        assertDoesNotThrow(
                () -> SoldeCongeService.validateAdjustment(20, 5)
        );
    }

    @Test
    void adjustmentRequiresBothValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SoldeCongeService.validateAdjustment(null, 0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> SoldeCongeService.validateAdjustment(20, null)
        );
    }

    @Test
    void yearValidationRejectsUnsupportedYear() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SoldeCongeService.validateYear(1999)
        );
    }
}
