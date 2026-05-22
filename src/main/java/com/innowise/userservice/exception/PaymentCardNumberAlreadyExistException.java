package com.innowise.userservice.exception;

public class PaymentCardNumberAlreadyExistException extends RuntimeException{
    private static final String MESSAGE_TEMPLATE = "Card with number %s already exists";

    public PaymentCardNumberAlreadyExistException(String number) {
        super(createErrorMessage(number));
    }

    public static String createErrorMessage(String number) {
        return String.format(MESSAGE_TEMPLATE, number);
    }

}