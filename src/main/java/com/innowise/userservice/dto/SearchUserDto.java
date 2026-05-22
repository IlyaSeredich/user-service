package com.innowise.userservice.dto;

import jakarta.validation.constraints.Size;

public record SearchUserDto(
        @Size(max = 50, message = "Name can not exceed 50 characters")
        String name,
        @Size(max = 50, message = "Surname can not exceed 50 characters")
        String surname) {
}
