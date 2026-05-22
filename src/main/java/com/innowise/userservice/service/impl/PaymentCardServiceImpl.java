package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.*;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.*;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final PaymentCardMapper paymentCardMapper;
    private final UserService userService;

    @Override
    @Transactional
    public PaymentCardResponseDto createPaymentCard(PaymentCardCreateDto createDto) {
        validateCreatingConditions(createDto.userId(), createDto.number());
        User user = userService.getUserEntity(createDto.userId());
        PaymentCard paymentCard = paymentCardMapper.toPaymentCard(createDto, user);
        paymentCard.setActive(true);
        PaymentCard savedPaymentCard = paymentCardRepository.save(paymentCard);
        return paymentCardMapper.toDto(savedPaymentCard);
    }

    @Override
    @Cacheable(value = "paymentCards", key = "#id")
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
    @CacheEvict(value = "paymentCards", key = "#id")
    public PaymentCardResponseDto updatePaymentCard(Long id, PaymentCardUpdateDto paymentCardUpdateDto) {
        PaymentCard paymentCard = getPaymentCardEntity(id);
        validateNumberForUpdating(id, paymentCardUpdateDto.number());
        paymentCardMapper.updatePaymentCard(paymentCardUpdateDto, paymentCard);
        paymentCardRepository.save(paymentCard);
        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    @Transactional
    @CacheEvict(value = "paymentCards", key = "#id")
    public void activatePaymentCard(Long id) {
        PaymentCard paymentCard = getPaymentCardEntity(id);
        if(paymentCard.getActive()) throw new PaymentCardAlreadyActiveException();
        paymentCard.setActive(true);
        paymentCardRepository.save(paymentCard);
    }

    @Override
    @Transactional
    @CacheEvict(value = "paymentCards", key = "#id")
    public void deactivatePaymentCard(Long id) {
        PaymentCard paymentCard = getPaymentCardEntity(id);
        if(!paymentCard.getActive()) throw new PaymentCardAlreadyNonActiveException();
        paymentCard.setActive(false);
        paymentCardRepository.save(paymentCard);
    }

    @Override
    public List<PaymentCardResponseDto> createPaymentCardResponseDtoList(List<PaymentCard> paymentCards) {
        return paymentCards.stream().map(paymentCardMapper::toDto).toList();
    }

    private PaymentCard getPaymentCardEntity(Long id) {
        return paymentCardRepository.findPaymentCardById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
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

    private void validateCreatingConditions(Long userId, String number) {
        validatePaymentCardLimit(userId);
        validateNumberNotExists(number);
    }

    private void validatePaymentCardLimit(Long userId) {
        long count = paymentCardRepository.countPaymentCardByUserId(userId);
        if (count >= 5) throw new PaymentCardLimitExceededException();
    }

    private void validateNumberForUpdating(Long id, String number) {
        if(number != null) {
            Optional<PaymentCard> paymentCard = paymentCardRepository.findByNumber(number);
            if(paymentCard.isPresent() && !paymentCard.get().getId().equals(id)) {
                throw new PaymentCardNumberAlreadyExistException(number);
            }
        }
    }

    private void validateNumberNotExists(String number) {
        if (paymentCardRepository.existsByNumber(number)) {
            throw new PaymentCardNumberAlreadyExistException(number);
        }
    }
}
