package com.innowise.userservice.service;

import com.innowise.userservice.dto.*;
import com.innowise.userservice.entity.PaymentCard;

import java.util.List;
import java.util.UUID;

public interface PaymentCardService {
    PaymentCardResponseDto createPaymentCard(PaymentCardCreateDto createDto);
    PaymentCardResponseDto getPaymentCard(Long id);
    PagePaymentCardResponseDto getAllPaymentCards(PageRequestDto pageRequestDto);
    List<PaymentCardResponseDto> getAllPaymentCards(UUID id);
    PaymentCardResponseDto updatePaymentCard(Long id, UUID userId, PaymentCardUpdateDto paymentCardUpdateDto);
    void activatePaymentCard(Long id, UUID userId);
    void deactivatePaymentCard(Long id, UUID userId);
    List<PaymentCardResponseDto> createPaymentCardResponseDtoList(List<PaymentCard> paymentCards);
}
