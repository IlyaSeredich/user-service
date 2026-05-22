package com.innowise.userservice.exception;

public class CardNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Card with id %d not found";

    public CardNotFoundException(Long id) {
        super(createErrorMessage(id));
    }

    public static String createErrorMessage(Long id) {
        return String.format(MESSAGE_TEMPLATE, id);
    }

}