package com.seredich.userservice.exception;

public class EmailAlreadyExistException extends RuntimeException{
    private static final String MESSAGE_TEMPLATE = "User with email %s already exists";

    public EmailAlreadyExistException(String email) {
        super(createErrorMessage(email));
    }

    public static String createErrorMessage(String email) {
        return String.format(MESSAGE_TEMPLATE, email);
    }

}