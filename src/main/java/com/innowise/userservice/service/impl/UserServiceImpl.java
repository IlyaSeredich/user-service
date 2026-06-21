package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.*;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.EmailAlreadyExistsException;
import com.innowise.userservice.exception.UserAlreadyActiveException;
import com.innowise.userservice.exception.UserAlreadyNonActiveException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.UserSpecification;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserSpecification userSpecification;

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        validateEmailNotExists(userCreateDto.email());
        User user = userMapper.toUser(userCreateDto);
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    @Transactional
    public UserResponseDto getUser(UUID id) {
        User user = getUserEntity(id);
        return userMapper.toDto(user);
    }

    @Override
    public PageUserResponseDto searchUsers(SearchUserDto searchUserDto, PageRequestDto pageRequestDto) {
        Pageable pageable = createPageable(pageRequestDto);
        Specification<User> specification = userSpecification.build(searchUserDto);
        Page<User> searchedUsers = userRepository.findAll(specification, pageable);
        return createPageUserResponseDto(searchedUsers);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDto updateUser(UserUpdateDto userUpdateDto, UUID id) {
        User user = getUserEntity(id);
        validateEmailForUpdating(id, userUpdateDto.email());
        userMapper.updateUser(userUpdateDto, user);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void activateUser(String id) {
        User user = getUserEntity(UUID.fromString(id));
        if(user.getActive()) throw new UserAlreadyActiveException();
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deactivateUser(String id) {
        User user = getUserEntity(UUID.fromString(id));
        if(!user.getActive()) throw new UserAlreadyNonActiveException();
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public User getUserEntity(UUID id) {
        return userRepository.findUserById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private Pageable createPageable(PageRequestDto pageRequestDto) {
        return PageRequest.of(
                pageRequestDto.pageNumber(),
                pageRequestDto.pageSize(),
                Sort.by(
                        Sort.Direction.fromString(pageRequestDto.sortDirection()),
                        pageRequestDto.sortField()
                ));
    }

    private PageUserResponseDto createPageUserResponseDto(Page<User> searchedUsers) {
        return new PageUserResponseDto(
                searchedUsers.getContent().stream().map(userMapper::toDto).toList(),
                searchedUsers.getPageable().getPageNumber(),
                searchedUsers.getPageable().getPageSize(),
                searchedUsers.getTotalElements(),
                searchedUsers.getTotalPages()
        );
    }

    private void validateEmailForUpdating(UUID id, String email) {
        if(email != null) {
            Optional<User> user = userRepository.findByEmail(email);
            if(user.isPresent() && !user.get().getId().equals(id)) {
                throw new EmailAlreadyExistsException(email);
            }
        }
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsUserByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }
}
