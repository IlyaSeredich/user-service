package com.seredich.userservice.service.impl;

import com.seredich.userservice.dto.*;
import com.seredich.userservice.entity.PaymentCard;
import com.seredich.userservice.mapper.PaymentCardMapper;
import com.seredich.userservice.repository.PaymentCardRepository;
import com.seredich.userservice.service.PaymentCardService;
import com.seredich.userservice.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final UserService userService;
    private final PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    public PaymentCardResponseDto createPaymentCard(PaymentCardCreateDto createDto) {
        if(userService.canAddPaymentCard(createDto.userId())) {
            PaymentCard paymentCard = paymentCardMapper.toPaymentCard(createDto);
            PaymentCard savedPaymentCard = paymentCardRepository.save(paymentCard);
            return paymentCardMapper.toDto(savedPaymentCard);
        } else {
            return null;
        }
    }

    @Override
    public PaymentCardResponseDto getPaymentCard(Long id) {
        PaymentCard paymentCard = getPaymentCardEntity(id);
        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    public PagePaymentCardResponseDto getAllPaymentCards(PageRequestDto pageRequestDto) {
        Pageable pageable = createPageable(pageRequestDto);
        Page<PaymentCard> allPaymentCards = paymentCardRepository.findAll(pageable);
        return createPagePaymentCardResponseDto(allPaymentCards);
    }

    @Override
    public List<PaymentCardResponseDto> getAllPaymentCards(Long userId) {
        return paymentCardRepository.findAllByUserId(userId).stream().map(paymentCardMapper::toDto).toList();
    }

    @Override
    @Transactional
    public PaymentCardResponseDto updatePaymentCard(Long id, PaymentCardUpdateDto paymentCardUpdateDto) {
        PaymentCard paymentCard = getPaymentCardEntity(id);
        paymentCardMapper.updatePaymentCard(paymentCardUpdateDto, paymentCard);
        paymentCardRepository.save(paymentCard);
        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    @Transactional
    public void activatePaymentCard(Long id) {
        PaymentCard paymentCard = getPaymentCardEntity(id);
        paymentCard.setActive(true);
        paymentCardRepository.save(paymentCard);
    }

    @Override
    @Transactional
    public void deactivatePaymentCard(Long id) {
        PaymentCard paymentCard = getPaymentCardEntity(id);
        paymentCard.setActive(false);
        paymentCardRepository.save(paymentCard);
    }

    private PaymentCard getPaymentCardEntity(Long id) {
        Optional<PaymentCard> paymentCard = paymentCardRepository.findPaymentCardById(id);
        return paymentCard.get();
    }

    private Pageable createPageable(PageRequestDto pageRequestDto) {
        return PageRequest.of(
                pageRequestDto.pageNumber(),
                pageRequestDto.pageSize(),
                Sort.by(
                        Sort.Direction.fromString(pageRequestDto.sortDirection()),
                        pageRequestDto.sortField()
                ));
    }

    private PagePaymentCardResponseDto createPagePaymentCardResponseDto(Page<PaymentCard> searchedCards) {
        return new PagePaymentCardResponseDto(
                searchedCards.getContent().stream().map(paymentCardMapper::toDto).toList(),
                searchedCards.getPageable().getPageNumber(),
                searchedCards.getPageable().getPageSize(),
                searchedCards.getTotalElements(),
                searchedCards.getTotalPages()
        );
    }

}
