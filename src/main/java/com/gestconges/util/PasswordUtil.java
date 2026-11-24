package com.gestconges.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hash a password before storing it in the database
    public static String hashPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty"
            );
        }

        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Verify a password against its stored hash
    public static boolean verifyPassword(
            String password,
            String hashedPassword
    ) {

        if (password == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}