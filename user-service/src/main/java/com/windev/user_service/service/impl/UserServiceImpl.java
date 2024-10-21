package com.windev.user_service.service.impl;

import com.windev.user_service.dto.request.UpdateUserRequest;
import com.windev.user_service.dto.response.PaginatedResponseDto;
import com.windev.user_service.dto.response.UserDto;
import com.windev.user_service.entity.Role;
import com.windev.user_service.entity.User;
import com.windev.user_service.exception.GlobalException;
import com.windev.user_service.mapper.UserMapper;
import com.windev.user_service.repository.RoleRepository;
import com.windev.user_service.repository.UserRepository;
import com.windev.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;



    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new GlobalException("--> USER NOT FOUND WITH ID: " + userId + "!", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<UserDto> getAllUsers(Pageable pageable) {
        return convertToPaginatedResponseDto(userRepository.findAll(pageable));
    }

    @Override
    @Transactional
    public UserDto updateCurrentUser(UpdateUserRequest updateUserRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new GlobalException("--> USER NOT FOUND WITH USERNAME: " + username + "!", HttpStatus.NOT_FOUND));

        userMapper.updateUserFromDto(updateUserRequest, user);

        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new GlobalException("--> USER NOT FOUND WITH USERNAME: " + username + "!", HttpStatus.NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserDto changeUserRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException("--> USER NOT FOUND WITH ID: " + userId + "!", HttpStatus.NOT_FOUND));

        Role role = roleRepository.findById(roleId).orElseThrow(() -> new GlobalException("--> ROLE NOT FOUND WITH ID: " + roleId,HttpStatus.NOT_FOUND ));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        user.setRoles(roles);

        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<UserDto> getUsersByRole(Set<Role> roles, Pageable pageable) {
        return convertToPaginatedResponseDto(userRepository.findByRoles(roles, pageable));
    }

    @Override
    @Transactional
    public UserDto disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(false);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    private PaginatedResponseDto<UserDto> convertToPaginatedResponseDto(Page<User> userPage) {
        List<UserDto> users = userPage.getContent().stream().map(userMapper::toDto).toList();
        return new PaginatedResponseDto<>(users, userPage.getNumber(), userPage.getSize(),
                userPage.getTotalPages(), userPage.getTotalElements(), userPage.isLast());
    }
}
