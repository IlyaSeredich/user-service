package com.seredich.userservice.service;

import com.seredich.userservice.dto.*;

public interface UserService {
    UserResponseDto createUser(UserCreateDto userCreateDto);
    UserResponseDto getUser(Long id);
    PageUserResponseDto searchUsers(SearchUserDto searchUserDto, PageUserRequestDto pageUserRequestDto);
    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);
    void activateUser(Long id);
    void deactivateUser(Long id);
}
