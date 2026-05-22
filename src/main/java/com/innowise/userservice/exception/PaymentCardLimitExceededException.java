package com.innowise.userservice.exception;

public class PaymentCardLimitExceededException extends RuntimeException{
    private static final String MESSAGE = "User already has 5 cards";

    public PaymentCardLimitExceededException() {
        super(createErrorMessage());
    }

    public static String createErrorMessage() {
        return MESSAGE;
    }

}