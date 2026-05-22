package com.innowise.userservice.exception;

public class PaymentCardAlreadyActiveException extends RuntimeException{
    private static final String MESSAGE = "Card already activated";

    public PaymentCardAlreadyActiveException() {
        super(createErrorMessage());
    }

    public static String createErrorMessage() {
        return MESSAGE;
    }

}