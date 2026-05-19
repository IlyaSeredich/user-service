package com.seredich.userservice.dto;

public record PaymentCardCreateDto(
        String number,
        String holder,
        String expirationDate,
        Long userId
) {
}
