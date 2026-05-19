package com.seredich.userservice.dto;

import jakarta.validation.constraints.Size;

public record PaymentCardUpdateDto(
        @Size(min = 10, max = 30, message = "Number must be between 10 and 30 characters long")
        String number,
        @Size(min = 5, max = 100, message = "Holder must be between 5 and 100 characters long")
        String holder,
        @Size(min = 5, max = 5, message = "Expiration date must have 5 characters")
        String expirationDate
) {
}
