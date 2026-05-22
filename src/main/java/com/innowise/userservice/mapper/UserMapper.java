package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.dto.UserResponseDto;
import com.innowise.userservice.dto.UserUpdateDto;
import com.innowise.userservice.entity.User;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = PaymentCardMapper.class
)
public interface UserMapper {

    User toUser(UserCreateDto userCreateDto);

    @Mapping(target = "paymentCards",
            source = "paymentCards",
            qualifiedByName = "toPaymentCardResponseDto")
    UserResponseDto toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(UserUpdateDto userUpdateDto, @MappingTarget User user);
}
