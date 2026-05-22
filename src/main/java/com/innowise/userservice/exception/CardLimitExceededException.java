package com.innowise.userservice.exception;

public class CardLimitExceededException extends RuntimeException{
    private static final String MESSAGE = "User already has 5 cards";

    public CardLimitExceededException() {
        super(createErrorMessage());
    }

    public static String createErrorMessage() {
        return MESSAGE;
    }

}