package com.innowise.userservice.controller;

import com.innowise.userservice.dto.*;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateDto userCreateDto,
            @AuthenticationPrincipal Jwt jwt
            ) {
        UserResponseDto userResponseDto = userService.createUser(userCreateDto, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal Jwt jwt) {
        UserResponseDto userResponseDto = userService.getUser(UUID.fromString(jwt.getSubject()));
        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping
    public ResponseEntity<PageUserResponseDto> searchUsers(
            @Valid @ModelAttribute SearchUserDto searchUserDto,
            @Valid @ModelAttribute PageRequestDto pageRequestDto
    ) {
        PageUserResponseDto searchedUsers = userService.searchUsers(searchUserDto, pageRequestDto);
        return ResponseEntity.ok(searchedUsers);
    }

    @PatchMapping
    public ResponseEntity<UserResponseDto> updateUser(
            @Valid @RequestBody UserUpdateDto userUpdateDto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserResponseDto userResponseDto =
                userService.updateUser(userUpdateDto, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.ok(userResponseDto);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(
            @PathVariable(name = "id") @org.hibernate.validator.constraints.UUID String id
    ) {
        userService.activateUser(UUID.fromString(id));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable(name = "id") @org.hibernate.validator.constraints.UUID String id
    ) {
        userService.deactivateUser(UUID.fromString(id));
        return ResponseEntity.ok().build();
    }
}
