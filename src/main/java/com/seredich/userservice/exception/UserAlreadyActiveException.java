package com.seredich.userservice.exception;

public class UserAlreadyActiveException extends RuntimeException{
    private static final String MESSAGE = "User already activated";

    public UserAlreadyActiveException() {
        super(createErrorMessage());
    }

    public static String createErrorMessage() {
        return MESSAGE;
    }

}