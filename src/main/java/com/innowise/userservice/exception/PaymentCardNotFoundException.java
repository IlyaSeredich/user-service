package com.innowise.userservice.exception;

public class PaymentCardNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Card with id %d not found";

    public PaymentCardNotFoundException(Long id) {
        super(createErrorMessage(id));
    }

    public static String createErrorMessage(Long id) {
        return String.format(MESSAGE_TEMPLATE, id);
    }

}