package com.innowise.userservice.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record UserCreateDto(
        @NotNull(message = "Id must not be null")
        UUID id,
        @NotBlank(message = "Name must not be blank")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters long")
        String name,
        @NotBlank(message = "Surname must not be blank")
        @Size(min = 3, max = 50, message = "Surname must be between 3 and 50 characters long")
        String surname,
        @NotNull(message = "Birthdate must not be null")
        @Past(message = "Birthdate must be in the past")
        LocalDate birthDate,
        @NotBlank(message = "Email must not be blank")
        @Size(min = 6, max = 100, message = "Email must be between 6 and 100 characters long")
        @Email(message = "Email should be valid")
        String email
) {
}
