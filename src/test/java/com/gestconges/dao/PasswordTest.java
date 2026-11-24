package com.gestconges.dao;

import com.gestconges.util.PasswordUtil;

public class PasswordTest {

    public static void main(String[] args) {

        String password = "Test1234";

        String hash = PasswordUtil.hashPassword(password);

        System.out.println("Generated hash: " + hash);

        System.out.println(
                "Correct password: "
                        + PasswordUtil.verifyPassword(password, hash)
        );

        System.out.println(
                "Incorrect password: "
                        + PasswordUtil.verifyPassword("Wrong123!", hash)
        );
    }
}