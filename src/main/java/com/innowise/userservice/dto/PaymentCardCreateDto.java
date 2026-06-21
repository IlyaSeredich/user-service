package com.innowise.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PaymentCardCreateDto(
        @NotBlank(message = "Number must not be blank")
        @Size(min = 10, max = 30, message = "Number must be between 10 and 30 characters long")
        String number,
        @NotBlank(message = "Holder must not be blank")
        @Size(min = 5, max = 100, message = "Holder must be between 5 and 100 characters long")
        String holder,
        @NotBlank(message = "Expiration date must not be blank")
        @Size(min = 5, max = 5, message = "Expiration date must have 5 characters")
        String expirationDate,
        @NotNull(message = "User id must not be null")
        UUID userId
) {
}
