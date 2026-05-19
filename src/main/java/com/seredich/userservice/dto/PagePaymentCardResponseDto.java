package com.seredich.userservice.dto;

import java.util.List;

public record PagePaymentCardResponseDto(
        List<PaymentCardResponseDto> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
){
}
