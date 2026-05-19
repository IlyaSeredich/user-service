package com.seredich.userservice.service;

import com.seredich.userservice.dto.*;

import java.util.List;

public interface PaymentCardService {
    PaymentCardResponseDto createPaymentCard(PaymentCardCreateDto createDto);
    PaymentCardResponseDto getPaymentCard(Long id);
    PagePaymentCardResponseDto getAllPaymentCards(PageRequestDto pageRequestDto);
    List<PaymentCardResponseDto> getAllPaymentCards(Long userId);
    PaymentCardResponseDto updatePaymentCard(Long id, PaymentCardUpdateDto paymentCardUpdateDto);
    void activatePaymentCard(Long id);
    void deactivatePaymentCard(Long id);
}
