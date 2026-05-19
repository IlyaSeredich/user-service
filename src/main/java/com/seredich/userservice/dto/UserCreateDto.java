package com.seredich.userservice.dto;

import java.time.LocalDate;

public record UserCreateDto(
        String name,
        String surname,
        LocalDate birthdate,
        String email
) {
}
