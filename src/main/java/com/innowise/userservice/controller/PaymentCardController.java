package com.innowise.userservice.controller;

import com.innowise.userservice.dto.*;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@AllArgsConstructor
@Validated
public class PaymentCardController {
    private final PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<PaymentCardResponseDto> createCard(
            @Valid @RequestBody PaymentCardCreateDto paymentCardCreateDto) {
        PaymentCardResponseDto paymentCard = paymentCardService.createPaymentCard(paymentCardCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCard);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> getCard(
            @PathVariable(name = "id") @Min(1) Long id
    ) {
        PaymentCardResponseDto paymentCard = paymentCardService.getPaymentCard(id);
        return ResponseEntity.ok(paymentCard);
    }

    @GetMapping
    public ResponseEntity<PagePaymentCardResponseDto> getAllCards(
            @Valid @ModelAttribute PageRequestDto pageRequestDto
            ) {
        PagePaymentCardResponseDto allPaymentCards = paymentCardService.getAllPaymentCards(pageRequestDto);
        return ResponseEntity.ok(allPaymentCards);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardResponseDto>> getUserCards(
            @PathVariable(name = "userId") @Min(1) Long userId
    ) {
        List<PaymentCardResponseDto> allUserCards = paymentCardService.getAllPaymentCards(userId);
        return ResponseEntity.ok(allUserCards);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> updateCard(
            @PathVariable(name = "id") @Min(1) Long id,
            @RequestBody @Valid PaymentCardUpdateDto paymentCardUpdateDto
            ) {
        PaymentCardResponseDto paymentCardResponseDto =
                paymentCardService.updatePaymentCard(id, paymentCardUpdateDto);
        return ResponseEntity.ok(paymentCardResponseDto);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCard(@PathVariable(name = "id") @Min(1) Long id) {
        paymentCardService.activatePaymentCard(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCard(@PathVariable(name = "id") @Min(1) Long id) {
        paymentCardService.deactivatePaymentCard(id);
        return ResponseEntity.ok().build();
    }
}
