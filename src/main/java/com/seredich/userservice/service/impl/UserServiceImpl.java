package com.seredich.userservice.service.impl;

import com.seredich.userservice.dto.*;
import com.seredich.userservice.entity.User;
import com.seredich.userservice.mapper.UserMapper;
import com.seredich.userservice.repository.UserRepository;
import com.seredich.userservice.service.UserService;
import com.seredich.userservice.specification.UserSpecification;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        User user = userMapper.toUser(userCreateDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto getUser(Long id) {
        User user = getUserEntity(id);
        return userMapper.toDto(user);
    }

    @Override
    public PageUserResponseDto searchUsers(SearchUserDto searchUserDto, PageRequestDto pageRequestDto) {
        Pageable pageable = createPageable(pageRequestDto);
        Specification<User> specification = UserSpecification.build(searchUserDto);
        Page<User> searchedUsers = userRepository.findAll(specification, pageable);
        return createPageUserResponseDto(searchedUsers);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = getUserEntity(id);
        userMapper.updateUser(userUpdateDto, user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        User user = getUserEntity(id);
        user.setActive(true);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = getUserEntity(id);
        user.setActive(false);
    }

    @Override
    public boolean canAddPaymentCard(Long userId) {
        User user = getUserEntity(userId);
        return user.getPaymentCards().size() < 5;
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

    private User getUserEntity(Long id) {
        Optional<User> optionalUser = userRepository.findUserById(id);
        return optionalUser.get();
    }
}
