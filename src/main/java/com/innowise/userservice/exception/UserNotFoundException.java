package com.innowise.userservice.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "User with id %s not found";

    public UserNotFoundException(UUID id) {
        super(createErrorMessage(id));
    }

    public static String createErrorMessage(UUID id) {
        return String.format(MESSAGE_TEMPLATE, id);
    }

}