package com.innowise.userservice.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record UserResponseDto(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        Boolean active,
        List<PaymentCardResponseDto> paymentCards
) implements Serializable {
}
