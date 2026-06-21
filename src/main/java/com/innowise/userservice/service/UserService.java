package com.innowise.userservice.service;

import com.innowise.userservice.dto.*;
import com.innowise.userservice.entity.User;

import java.util.UUID;

public interface UserService {
    UserResponseDto createUser(UserCreateDto userCreateDto);
    UserResponseDto getUser(UUID id);
    PageUserResponseDto searchUsers(SearchUserDto searchUserDto, PageRequestDto pageRequestDto);
    UserResponseDto updateUser(UserUpdateDto userUpdateDto, UUID id);
    User getUserEntity(UUID id);
    void activateUser(String id);
    void deactivateUser(String id);
}
