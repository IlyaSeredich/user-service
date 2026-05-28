package com.seredich.userservice.dto;

import java.io.Serializable;
import java.time.LocalDate;

public record UserResponseDto(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        Boolean active
) implements Serializable {
}
