package com.seredich.userservice.exception;

public class CardNumberAlreadyExistException extends RuntimeException{
    private static final String MESSAGE_TEMPLATE = "Card with number %s already exists";

    public CardNumberAlreadyExistException(String number) {
        super(createErrorMessage(number));
    }

    public static String createErrorMessage(String number) {
        return String.format(MESSAGE_TEMPLATE, number);
    }

}