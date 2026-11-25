package com.gestconges.service;

import com.gestconges.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeServiceTest {

    private final EmployeService employeService = new EmployeService();

    @Test
    void createEmployeeRejectsMissingRequiredFieldsBeforeDatabaseAccess() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeService.createEmployee(
                        " ",
                        "Nom",
                        "Prenom",
                        "employee@example.test",
                        "initial-password",
                        Role.EMPLOYE,
                        null,
                        1L
                )
        );

        assertTrue(exception.getMessage().contains("matricule"));
    }

    @Test
    void createEmployeeRejectsInvalidEmailBeforeDatabaseAccess() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeService.createEmployee(
                        "EMP-1",
                        "Nom",
                        "Prenom",
                        "invalid-email",
                        "initial-password",
                        Role.EMPLOYE,
                        null,
                        1L
                )
        );

        assertTrue(exception.getMessage().contains("email valide"));
    }

    @Test
    void createEmployeeRequiresDepartmentBeforeDatabaseAccess() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeService.createEmployee(
                        "EMP-1",
                        "Nom",
                        "Prenom",
                        "employee@example.test",
                        "initial-password",
                        Role.EMPLOYE,
                        null,
                        null
                )
        );

        assertTrue(exception.getMessage().contains("département"));
    }
}
