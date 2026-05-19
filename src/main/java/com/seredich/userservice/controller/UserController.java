package com.seredich.userservice.controller;

import com.seredich.userservice.dto.*;
import com.seredich.userservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        UserResponseDto userResponseDto = userService.createUser(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable(name = "id") @Min(1) Long id) {
        UserResponseDto userResponseDto = userService.getUser(id);
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

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable(name = "id") @Min(1) Long id,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto userResponseDto = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(userResponseDto);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable(name = "id") @Min(1)  Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable(name = "id") @Min(1)  Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }
}
