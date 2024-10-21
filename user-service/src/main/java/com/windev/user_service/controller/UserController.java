package com.windev.user_service.controller;

import com.windev.user_service.dto.request.UpdateUserRequest;
import com.windev.user_service.dto.response.PaginatedResponseDto;
import com.windev.user_service.dto.response.UserDto;
import com.windev.user_service.entity.Role;
import com.windev.user_service.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<UserDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(@RequestBody UpdateUserRequest updateUserRequest) {
        UserDto updatedUser = userService.updateCurrentUser(updateUserRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUser() {
        userService.deleteCurrentUser();
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long userId, @PathVariable Long roleId) {
        UserDto updatedUser = userService.changeUserRole(userId, roleId);
        return ResponseEntity.ok(updatedUser);
    }

//    @GetMapping("/roles")
//    public ResponseEntity<PaginatedResponseDto<UserDto>> getUsersByRole(@RequestParam Set<Long> roleIds, Pageable pageable) {
//        Set<Role> roles = roleIds.stream().map(id -> new Role(id)).collect(Collectors.toSet());
//        return ResponseEntity.ok(userService.getUsersByRole(roles, pageable));
//    }

    @PostMapping("/{userId}/disable")
    public ResponseEntity<UserDto> disableUser(@PathVariable Long userId) {
        UserDto user = userService.disableUser(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/enable")
    public ResponseEntity<UserDto> enableUser(@PathVariable Long userId) {
        UserDto user = userService.enableUser(userId);
        return ResponseEntity.ok(user);
    }
}
