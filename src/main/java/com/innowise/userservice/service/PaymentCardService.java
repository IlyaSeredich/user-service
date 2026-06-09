package com.innowise.userservice.service;

import com.innowise.userservice.dto.*;

import java.util.List;
import java.util.UUID;

public interface PaymentCardService {
    PaymentCardResponseDto createPaymentCard(PaymentCardCreateDto createDto);
    PaymentCardResponseDto getPaymentCard(Long id);
    PagePaymentCardResponseDto getAllPaymentCards(PageRequestDto pageRequestDto);
    List<PaymentCardResponseDto> getAllPaymentCards(UUID userId);
    PaymentCardResponseDto updatePaymentCard(Long id, PaymentCardUpdateDto paymentCardUpdateDto);
    void activatePaymentCard(Long id);
    void deactivatePaymentCard(Long id);
}
