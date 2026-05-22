package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.PaymentCardCreateDto;
import com.innowise.userservice.dto.PaymentCardResponseDto;
import com.innowise.userservice.dto.PaymentCardUpdateDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentCard toPaymentCard(PaymentCardCreateDto createDto, User user);
    @Named("toPaymentCardResponseDto")
    @Mapping(target = "userId", expression = "java(paymentCard.getUser().getId())")
    PaymentCardResponseDto toDto(PaymentCard paymentCard);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePaymentCard(PaymentCardUpdateDto updateDto, @MappingTarget PaymentCard paymentCard);
}
