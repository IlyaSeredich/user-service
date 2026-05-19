package com.seredich.userservice.dto;

public record PaymentCardUpdateDto(
        String number,
        String holder,
        String expirationDate
) {
}
