package com.innowise.userservice.exception;

public class PaymentCardAlreadyNonActiveException extends RuntimeException{
    private static final String MESSAGE = "Card already deactivated";

    public PaymentCardAlreadyNonActiveException() {
        super(createErrorMessage());
    }

    public static String createErrorMessage() {
        return MESSAGE;
    }

}