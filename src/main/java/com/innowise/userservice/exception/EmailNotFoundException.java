package com.innowise.userservice.exception;

import java.util.UUID;

public class EmailNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "User with email %s not found";

    public EmailNotFoundException(String email) {
        super(createErrorMessage(email));
    }

    public static String createErrorMessage(String email) {
        return String.format(MESSAGE_TEMPLATE, email);
    }

}