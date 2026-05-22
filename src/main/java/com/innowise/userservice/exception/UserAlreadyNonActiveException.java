package com.innowise.userservice.exception;

public class UserAlreadyNonActiveException extends RuntimeException{
    private static final String MESSAGE = "User already deactivated";

    public UserAlreadyNonActiveException() {
        super(createErrorMessage());
    }

    public static String createErrorMessage() {
        return MESSAGE;
    }

}