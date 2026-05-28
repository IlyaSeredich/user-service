package com.seredich.userservice.dto;

import java.io.Serializable;

public record PaymentCardResponseDto(
        Long id,
        String number,
        String holder,
        String expirationDate,
        Long userId,
        Boolean active
) implements Serializable {
}
