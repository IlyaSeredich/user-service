package com.innowise.userservice.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        Boolean active
) implements Serializable {
}
