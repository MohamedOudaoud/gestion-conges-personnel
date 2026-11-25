package com.gestconges.service;

import com.gestconges.model.StatutDemande;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryDashboardServiceTest {

    @Test
    void emptyStatusFilterMeansAllStatuses() {
        assertNull(HistoryDashboardService.parseStatus(" "));
    }

    @Test
    void validStatusFilterIsParsed() {
        assertEquals(
                StatutDemande.ACCEPTEE,
                HistoryDashboardService.parseStatus("ACCEPTEE")
        );
    }

    @Test
    void invalidStatusFilterIsRejected() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> HistoryDashboardService.parseStatus("UNKNOWN")
        );
        assertTrue(exception.getMessage().contains("invalide"));
    }

    @Test
    void filterDatesRequireEndAfterOrEqualToStart() {
        LocalDate start = LocalDate.of(2026, 12, 10);
        LocalDate end = LocalDate.of(2026, 12, 9);

        assertThrows(
                IllegalArgumentException.class,
                () -> HistoryDashboardService.validateFilterDates(
                        start,
                        end
                )
        );
    }

    @Test
    void equalFilterDatesAreAccepted() {
        LocalDate date = LocalDate.of(2026, 12, 10);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> HistoryDashboardService.validateFilterDates(
                        date,
                        date
                )
        );
    }
}
