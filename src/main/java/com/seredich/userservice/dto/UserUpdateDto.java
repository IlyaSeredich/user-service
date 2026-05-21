package com.seredich.userservice.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserUpdateDto(
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters long")
        String name,
        @Size(min = 3, max = 50, message = "Surname must be between 3 and 50 characters long")
        String surname,
        @Past(message = "Birthdate must be in the past")
        LocalDate birthDate,
        @Size(min = 6, max = 100, message = "Email must be between 6 and 100 characters long")
        @Email(message = "Email should be valid")
        String email
) {
}
