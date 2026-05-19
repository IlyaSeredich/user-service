package com.seredich.userservice.dto;

import java.time.LocalDate;

public record UserUpdateDto(
        String name,
        String surname,
        LocalDate birthDate,
        String email
) {
}
