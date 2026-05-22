package com.seredich.userservice.service.impl;

import com.seredich.userservice.dto.UserResponseDto;
import com.seredich.userservice.mapper.PaymentCardMapper;
import com.seredich.userservice.repository.PaymentCardRepository;
import com.seredich.userservice.service.PaymentCardService;
import com.seredich.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                PaymentCardServiceImpl.class
        }
)
class PaymentCardServiceImplTest {
    @MockitoBean
    private PaymentCardRepository paymentCardRepository;
    @MockitoBean
    private PaymentCardMapper paymentCardMapper;
    @MockitoBean
    private UserService userService;
    @Autowired
    private PaymentCardService paymentCardService;





}