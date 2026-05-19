package com.seredich.userservice.dto;

import java.util.List;

public record PageUserResponseDto(
        List<UserResponseDto> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
){
}
