package com.seredich.userservice.dto;

public record PaymentCardResponseDto(
        Long id,
        String number,
        String holder,
        String expirationDate,
        Long userId,
        Boolean active
) {
}
