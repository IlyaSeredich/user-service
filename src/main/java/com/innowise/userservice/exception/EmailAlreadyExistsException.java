package com.innowise.userservice.exception;

public class EmailAlreadyExistsException extends RuntimeException{
    private static final String MESSAGE_TEMPLATE = "User with email %s already exists";

    public EmailAlreadyExistsException(String email) {
        super(createErrorMessage(email));
    }

    public static String createErrorMessage(String email) {
        return String.format(MESSAGE_TEMPLATE, email);
    }

}