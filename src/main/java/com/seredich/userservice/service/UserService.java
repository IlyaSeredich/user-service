package com.seredich.userservice.service;

import com.seredich.userservice.dto.*;
import com.seredich.userservice.entity.User;

public interface UserService {
    UserResponseDto createUser(UserCreateDto userCreateDto);
    UserResponseDto getUser(Long id);
    PageUserResponseDto searchUsers(SearchUserDto searchUserDto, PageRequestDto pageRequestDto);
    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);
    User getUserEntity(Long id);
    void activateUser(Long id);
    void deactivateUser(Long id);
}
