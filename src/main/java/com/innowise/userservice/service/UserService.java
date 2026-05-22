package com.innowise.userservice.service;

import com.innowise.userservice.dto.*;
import com.innowise.userservice.entity.User;

public interface UserService {
    UserResponseDto createUser(UserCreateDto userCreateDto);
    UserResponseDto getUser(Long id);
    PageUserResponseDto searchUsers(SearchUserDto searchUserDto, PageRequestDto pageRequestDto);
    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);
    User getUserEntity(Long id);
    void activateUser(Long id);
    void deactivateUser(Long id);
}
