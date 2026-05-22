package com.innowise.userservice.service;

import com.innowise.userservice.dto.*;
import com.innowise.userservice.entity.PaymentCard;

import java.util.List;

public interface PaymentCardService {
    PaymentCardResponseDto createPaymentCard(PaymentCardCreateDto createDto);
    PaymentCardResponseDto getPaymentCard(Long id);
    PagePaymentCardResponseDto getAllPaymentCards(PageRequestDto pageRequestDto);
    List<PaymentCardResponseDto> getAllPaymentCards(Long userId);
    PaymentCardResponseDto updatePaymentCard(Long id, PaymentCardUpdateDto paymentCardUpdateDto);
    void activatePaymentCard(Long id);
    void deactivatePaymentCard(Long id);
    List<PaymentCardResponseDto> createPaymentCardResponseDtoList(List<PaymentCard> paymentCards);
}
