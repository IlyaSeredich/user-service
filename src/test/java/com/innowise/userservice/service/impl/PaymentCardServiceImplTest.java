package com.innowise.userservice.service.impl;


import com.innowise.userservice.dto.*;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.*;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                PaymentCardServiceImpl.class
        }
)
@ActiveProfiles("test")
class PaymentCardServiceImplTest {
    @MockitoBean
    private PaymentCardRepository paymentCardRepository;
    @MockitoBean
    private PaymentCardMapper paymentCardMapper;
    @MockitoBean
    private UserService userService;
    @Autowired
    private PaymentCardService paymentCardService;

    private User user;
    private PaymentCard paymentCard;
    private PaymentCardResponseDto responseDto;

    private static final Long CARD_ID = 1L;
    private static final Long CARD_ID_2 = 2L;
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String NUMBER = "1111 1111 1111 1111";
    private static final String NUMBER_2 = "2222 2222 2222 2222 ";
    private static final String HOLDER = "testName testSurname";
    private static final String HOLDER_2 = "testName2 testSurname2";
    private static final String EXPIRATION_DATE = "12/99";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setName("testName");
        user.setSurname("testSurname");
        user.setEmail("test@test.com");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setActive(true);


        paymentCard = new PaymentCard();
        paymentCard.setId(CARD_ID);
        paymentCard.setNumber(NUMBER);
        paymentCard.setUser(user);
        paymentCard.setActive(true);

        responseDto = new PaymentCardResponseDto(
                CARD_ID, NUMBER,
                HOLDER,
                EXPIRATION_DATE,
                USER_ID,
                true
        );
    }

    @Test
    void shouldCreateCard() {
        PaymentCardCreateDto createDto = new PaymentCardCreateDto(
                NUMBER,
                HOLDER,
                EXPIRATION_DATE,
                USER_ID
        );

        when(paymentCardRepository.countPaymentCardByUserId(USER_ID))
                .thenReturn(2L);

        when(paymentCardRepository.existsByNumber(NUMBER))
                .thenReturn(false);

        when(userService.getUserEntity(USER_ID))
                .thenReturn(user);

        when(paymentCardMapper.toPaymentCard(createDto, user))
                .thenReturn(paymentCard);

        when(paymentCardRepository.save(paymentCard))
                .thenReturn(paymentCard);

        when(paymentCardMapper.toDto(paymentCard))
                .thenReturn(responseDto);

        PaymentCardResponseDto result = paymentCardService.createPaymentCard(createDto);

        assertEquals(responseDto, result);
        assertTrue(paymentCard.getActive());

        verify(paymentCardRepository).countPaymentCardByUserId(USER_ID);
        verify(paymentCardRepository).existsByNumber(NUMBER);
        verify(userService).getUserEntity(USER_ID);
        verify(paymentCardMapper).toPaymentCard(createDto, user);
        verify(paymentCardRepository).save(paymentCard);
        verify(paymentCardMapper).toDto(paymentCard);
    }

    @Test
    void shouldRejectCreatingWhenCardLimitExceeded() {
        PaymentCardCreateDto createDto = new PaymentCardCreateDto(
                NUMBER,
                HOLDER,
                EXPIRATION_DATE,
                USER_ID
        );

        when(paymentCardRepository.countPaymentCardByUserId(USER_ID))
                .thenReturn(5L);

        assertThrows(
                PaymentCardLimitExceededException.class,
                () -> paymentCardService.createPaymentCard(createDto)
        );

        verify(paymentCardRepository).countPaymentCardByUserId(USER_ID);
    }

    @Test
    void shouldRejectCreatingWhenNumberAlreadyExists() {
        PaymentCardCreateDto createDto = new PaymentCardCreateDto(
                NUMBER,
                HOLDER,
                EXPIRATION_DATE,
                USER_ID
        );

        when(paymentCardRepository.countPaymentCardByUserId(USER_ID))
                .thenReturn(2L);

        when(paymentCardRepository.existsByNumber(NUMBER))
                .thenReturn(true);

        assertThrows(
                PaymentCardNumberAlreadyExistException.class,
                () -> paymentCardService.createPaymentCard(createDto)
        );

        verify(paymentCardRepository).countPaymentCardByUserId(USER_ID);
        verify(paymentCardRepository).existsByNumber(NUMBER);
    }

    @Test
    void shouldReturnCardById() {
        when(paymentCardRepository.findPaymentCardById(CARD_ID))
                .thenReturn(Optional.of(paymentCard));

        when(paymentCardMapper.toDto(paymentCard))
                .thenReturn(responseDto);

        PaymentCardResponseDto result = paymentCardService.getPaymentCard(CARD_ID);

        assertEquals(responseDto, result);
        verify(paymentCardRepository).findPaymentCardById(CARD_ID);
        verify(paymentCardMapper).toDto(paymentCard);
    }

    @Test
    void shouldReturnAllCards() {
        PageRequestDto pageRequestDto = new PageRequestDto(null, null, null, null);
        when(paymentCardRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(
                List.of(paymentCard),
                PageRequest.of(0, 10),
                1
        ));
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(responseDto);

        PagePaymentCardResponseDto result = paymentCardService.getAllPaymentCards(pageRequestDto);

        assertEquals(1, result.content().size());
        assertEquals(responseDto, result.content().getFirst());

        verify(paymentCardRepository).findAll(any(Pageable.class));
        verify(paymentCardMapper).toDto(paymentCard);
    }

    @Test
    void shouldRejectGettingWhenCardNotFound() {
        when(paymentCardRepository.findPaymentCardById(CARD_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentCardNotFoundException.class,
                () -> paymentCardService.getPaymentCard(CARD_ID)
        );

        verify(paymentCardRepository).findPaymentCardById(CARD_ID);
    }

    @Test
    void shouldReturnAllUsersCards() {
        when(paymentCardRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(paymentCard));

        when(paymentCardMapper.toDto(paymentCard))
                .thenReturn(responseDto);

        List<PaymentCardResponseDto> result = paymentCardService
                .getAllPaymentCards(USER_ID);

        assertEquals(1, result.size());

        verify(paymentCardRepository).findAllByUserId(USER_ID);
    }

    @Test
    void shouldUpdateCard() {
        PaymentCardUpdateDto updateDto = new PaymentCardUpdateDto(
                NUMBER_2,
                null,
                null
        );


        when(paymentCardRepository.findPaymentCardById(CARD_ID))
                .thenReturn(Optional.of(paymentCard));

        when(paymentCardRepository.findByNumber(NUMBER_2)).thenReturn(Optional.empty());

        when(paymentCardRepository.save(paymentCard))
                .thenReturn(paymentCard);

        when(paymentCardMapper.toDto(paymentCard))
                .thenReturn(responseDto);

        PaymentCardResponseDto result =
                paymentCardService.updatePaymentCard(CARD_ID, USER_ID, updateDto);

        assertEquals(responseDto, result);

        verify(paymentCardRepository).findByNumber(NUMBER_2);
        verify(paymentCardRepository).findPaymentCardById(CARD_ID);
        verify(paymentCardMapper).updatePaymentCard(updateDto, paymentCard);
        verify(paymentCardRepository).save(paymentCard);
    }

    @Test
    void shouldRejectUpdatingWhenNumberAlreadyExists() {
        PaymentCardUpdateDto updateDto = new PaymentCardUpdateDto(
                NUMBER_2,
                HOLDER_2,
                null);

        PaymentCard paymentCard2 = new PaymentCard();
        paymentCard2.setId(CARD_ID_2);
        paymentCard2.setNumber(NUMBER_2);

        when(paymentCardRepository.findPaymentCardById(CARD_ID))
                .thenReturn(Optional.of(paymentCard));

        when(paymentCardRepository.findByNumber(NUMBER_2))
                .thenReturn(Optional.of(paymentCard2));

        assertThrows(
                PaymentCardNumberAlreadyExistException.class,
                () -> paymentCardService.updatePaymentCard(CARD_ID,USER_ID, updateDto)
        );

        verify(paymentCardRepository).findPaymentCardById(CARD_ID);
        verify(paymentCardRepository).findByNumber(NUMBER_2);
    }

    @Test
    void shouldRejectUpdatingWhenCardNotFound() {
        PaymentCardUpdateDto updateDto = new PaymentCardUpdateDto(
                NUMBER_2,
                HOLDER_2,
                null
        );

        when(paymentCardRepository.findPaymentCardById(CARD_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentCardNotFoundException.class,
                () -> paymentCardService.updatePaymentCard(CARD_ID, USER_ID,updateDto)
        );

        verify(paymentCardRepository).findPaymentCardById(CARD_ID);
    }

    @Test
    void shouldActivateCard() {
        paymentCard.setActive(false);

        when(paymentCardRepository.findPaymentCardById(CARD_ID))
                .thenReturn(Optional.of(paymentCard));

        paymentCardService.activatePaymentCard(CARD_ID, USER_ID);

        assertTrue(paymentCard.getActive());
        verify(paymentCardRepository).save(paymentCard);
    }

    @Test
    void shouldRejectActivatingWhenCardAlreadyActive() {
        paymentCard.setActive(true);

        when(paymentCardRepository.findPaymentCardById(CARD_ID))
                .thenReturn(Optional.of(paymentCard));

        assertThrows(
                PaymentCardAlreadyActiveException.class,
                () -> paymentCardService.activatePaymentCard(CARD_ID, USER_ID)
        );

        verify(paymentCardRepository).findPaymentCardById(CARD_ID);
        verify(paymentCardRepository, never()).save(any());
    }


    @Test
    void shouldDeactivatePaymentCard() {
        paymentCard.setActive(true);

        when(paymentCardRepository.findPaymentCardById(CARD_ID))
                .thenReturn(Optional.of(paymentCard));

        paymentCardService.deactivatePaymentCard(CARD_ID, USER_ID);

        assertFalse(paymentCard.getActive());

        verify(paymentCardRepository).save(paymentCard);
    }

    @Test
    void shouldRejectDeactivatingWhenCardAlreadyNonActive() {
        paymentCard.setActive(false);
        when(paymentCardRepository.findPaymentCardById(CARD_ID))
                .thenReturn(Optional.of(paymentCard));

        assertThrows(
                PaymentCardAlreadyNonActiveException.class,
                () -> paymentCardService.deactivatePaymentCard(CARD_ID, USER_ID)
        );

        verify(paymentCardRepository).findPaymentCardById(CARD_ID);
        verify(paymentCardRepository, never()).save(any());
    }

}