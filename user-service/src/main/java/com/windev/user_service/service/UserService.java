package com.windev.user_service.service;

import com.windev.user_service.dto.request.UpdateUserRequest;
import com.windev.user_service.dto.response.PaginatedResponseDto;
import com.windev.user_service.dto.response.UserDto;
import com.windev.user_service.entity.Role;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface UserService {

    UserDto getUserById(Long userId);

    PaginatedResponseDto<UserDto> getAllUsers(Pageable pageable);

    UserDto updateCurrentUser(UpdateUserRequest updateUserRequest);

    void deleteCurrentUser();

    UserDto changeUserRole(Long userId, Long roleId);

    PaginatedResponseDto<UserDto> getUsersByRole(Set<Role> roles, Pageable pageable);

    UserDto disableUser(Long userId);

    UserDto enableUser(Long userId);
}
