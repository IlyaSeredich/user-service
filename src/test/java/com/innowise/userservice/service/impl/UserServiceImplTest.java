package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.dto.UserResponseDto;
import com.innowise.userservice.dto.UserUpdateDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.EmailAlreadyExistException;
import com.innowise.userservice.exception.UserAlreadyActiveException;
import com.innowise.userservice.exception.UserAlreadyNonActiveException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                UserServiceImpl.class
        }
)
@ActiveProfiles("test")
class UserServiceImplTest {

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private UserMapper userMapper;
    @Autowired
    private UserService userService;


    private User user;
    private UserResponseDto userResponseDto;
    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;

    private static final Long ID = 1L;
    private static final String NAME = "testName";
    private static final String SURNAME = "testSurname";
    private static final String EMAIL = "test@test.com";
    private static final LocalDate BIRTHDATE = LocalDate.of(2000, 1, 1);

    private static final String NAME2 = "testName2";
    private static final String SURNAME2 = "testSurname2";
    private static final String EMAIL2 = "test2@test.com";
    private static final LocalDate BIRTHDATE2 = LocalDate.of(2000, 1, 1);

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(ID);
        user.setName(NAME);
        user.setSurname(SURNAME);
        user.setEmail(EMAIL);
        user.setBirthDate(BIRTHDATE);
        user.setActive(true);

        userResponseDto = new UserResponseDto(
                ID,
                NAME,
                SURNAME,
                BIRTHDATE,
                EMAIL,
                true
        );

        userCreateDto = new UserCreateDto(
                NAME,
                SURNAME,
                BIRTHDATE,
                EMAIL
        );

        userUpdateDto = new UserUpdateDto(
                NAME2,
                SURNAME2,
                BIRTHDATE2,
                EMAIL2
        );

    }

    @Test
    void shouldCreateUser() {
        when(userRepository.existsUserByEmail(EMAIL))
                .thenReturn(false);

        when(userMapper.toUser(userCreateDto))
                .thenReturn(user);

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toDto(user))
                .thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userCreateDto);

        assertEquals(userResponseDto, result);

        verify(userRepository).existsUserByEmail(EMAIL);
        verify(userMapper).toUser(userCreateDto);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void shouldRejectCreatingWhenEmailExists() {
        when(userRepository.existsUserByEmail(EMAIL))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistException.class,
                () -> userService.createUser(userCreateDto)
        );

        verify(userRepository).existsUserByEmail(EMAIL);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnUser() {
        when(userRepository.findUserById(ID))
                .thenReturn(Optional.of(user));

        when(userMapper.toDto(user))
                .thenReturn(userResponseDto);

        UserResponseDto result = userService.getUser(ID);

        assertEquals(userResponseDto, result);

        verify(userRepository).findUserById(ID);
        verify(userMapper).toDto(user);
    }

    @Test
    void shouldRejectGettingWhenUserNotFound() {
        when(userRepository.findUserById(ID))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUser(ID)
        );

        verify(userRepository).findUserById(ID);
    }

    @Test
    void shouldUpdateUser() {
        when(userRepository.existsUserByEmail(EMAIL2))
                .thenReturn(false);

        when(userRepository.findUserById(ID))
                .thenReturn(Optional.of(user));

        when(userMapper.toDto(user))
                .thenReturn(userResponseDto);

        userService.updateUser(ID, userUpdateDto);

        verify(userRepository).existsUserByEmail(EMAIL2);
        verify(userRepository).findUserById(ID);
        verify(userMapper).updateUser(userUpdateDto, user);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void shouldRejectUpdatingWhenEmailExists() {
        when(userRepository.existsUserByEmail(EMAIL2))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistException.class,
                () -> userService.updateUser(ID, userUpdateDto)
        );

        verify(userRepository).existsUserByEmail(EMAIL2);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldActivateUser() {
        user.setActive(false);

        when(userRepository.findUserById(ID))
                .thenReturn(Optional.of(user));

        userService.activateUser(ID);

        assertTrue(user.getActive());

        verify(userRepository).findUserById(ID);
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectActivatingWhenAlreadyActive() {
        user.setActive(true);

        when(userRepository.findUserById(ID))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserAlreadyActiveException.class,
                () -> userService.activateUser(ID)
        );

        verify(userRepository).findUserById(ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeactivateUser() {
        user.setActive(true);

        when(userRepository.findUserById(ID))
                .thenReturn(Optional.of(user));

        userService.deactivateUser(ID);

        assertFalse(user.getActive());

        verify(userRepository).findUserById(ID);
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectDeactivatingWhenAlreadyNonActive() {
        user.setActive(false);

        when(userRepository.findUserById(ID))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserAlreadyNonActiveException.class,
                () -> userService.deactivateUser(ID)
        );

        verify(userRepository).findUserById(ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnEntity() {
        when(userRepository.findUserById(ID))
                .thenReturn(Optional.of(user));

        User result = userService.getUserEntity(ID);

        assertEquals(user, result);

        verify(userRepository).findUserById(ID);
    }

    @Test
    void shouldRejectGettingEntityWhenUserNotFound() {
        when(userRepository.findUserById(ID))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserEntity(ID)
        );

        verify(userRepository).findUserById(ID);
    }

}