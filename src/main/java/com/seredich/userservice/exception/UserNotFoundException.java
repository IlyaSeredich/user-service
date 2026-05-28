package com.seredich.userservice.exception;

public class UserNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "User with id %d not found";

    public UserNotFoundException(Long id) {
        super(createErrorMessage(id));
    }

    public static String createErrorMessage(Long id) {
        return String.format(MESSAGE_TEMPLATE, id);
    }

}