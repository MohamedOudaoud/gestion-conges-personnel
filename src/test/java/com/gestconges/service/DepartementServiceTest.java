package com.gestconges.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepartementServiceTest {

    private final DepartementService departementService =
            new DepartementService();

    @Test
    void createDepartmentRejectsBlankNameBeforeDatabaseAccess() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departementService.createDepartment("  ", null)
        );

        assertTrue(exception.getMessage().contains("nom"));
    }

    @Test
    void createDepartmentRejectsNameLongerThanDatabaseColumn() {
        String name = "D".repeat(256);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departementService.createDepartment(name, null)
        );

        assertTrue(exception.getMessage().contains("255 caractères"));
    }

    @Test
    void createDepartmentRejectsDescriptionLongerThanDatabaseColumn() {
        String description = "D".repeat(256);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departementService.createDepartment(
                        "Ressources humaines",
                        description
                )
        );

        assertTrue(exception.getMessage().contains("255 caractères"));
    }

    @Test
    void managerAssignmentRequiresASelectedEmployee() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departementService.assignManager(1L, 2L, List.of())
        );

        assertTrue(exception.getMessage().contains("au moins un employé"));
    }

    @Test
    void departmentDeletionRejectsInvalidIdBeforeDatabaseAccess() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departementService.deleteDepartment(0L)
        );

        assertTrue(exception.getMessage().contains("invalide"));
    }
}
