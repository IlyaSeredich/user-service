package com.seredich.userservice.mapper;

import com.seredich.userservice.dto.PaymentCardCreateDto;
import com.seredich.userservice.dto.PaymentCardResponseDto;
import com.seredich.userservice.dto.PaymentCardUpdateDto;
import com.seredich.userservice.entity.PaymentCard;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    PaymentCard toPaymentCard(PaymentCardCreateDto createDto);
    PaymentCardResponseDto toDto(PaymentCard paymentCard);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePaymentCard(PaymentCardUpdateDto updateDto, @MappingTarget PaymentCard paymentCard);
}
