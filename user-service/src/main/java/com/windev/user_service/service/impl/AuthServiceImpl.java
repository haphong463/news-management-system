package com.windev.user_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windev.user_service.config.CustomUserDetails;
import com.windev.user_service.constant.EventConstant;
import com.windev.user_service.dto.request.PasswordResetRequest;
import com.windev.user_service.dto.response.UserDto;
import com.windev.user_service.entity.Role;
import com.windev.user_service.entity.User;
import com.windev.user_service.event.EventMessage;
import com.windev.user_service.event.PasswordResetEvent;
import com.windev.user_service.event.UserRegisteredEvent;
import com.windev.user_service.exception.GlobalException;
import com.windev.user_service.exception.RoleNotFoundException;
import com.windev.user_service.mapper.UserMapper;
import com.windev.user_service.dto.request.SigninRequest;
import com.windev.user_service.dto.request.SignupRequest;
import com.windev.user_service.repository.RoleRepository;
import com.windev.user_service.repository.UserRepository;
import com.windev.user_service.service.AuthService;
import com.windev.user_service.service.EventPublisherService;
import com.windev.user_service.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final EventPublisherService eventPublisherService;


    @Override
    public UserDto registerUser(SignupRequest signupRequest) {
        User createdUser = createUser(signupRequest);

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .username(createdUser.getUsername())
                .email(createdUser.getEmail())
                .token(createdUser.getToken())
                .build();

        eventPublisherService.publishEvent(EventConstant.USER_REGISTERED, event);

        return userMapper.toDto(createdUser);
    }

    private User createUser(SignupRequest signupRequest) {
        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .token(UUID.randomUUID().toString())
                .enabled(false)
                .roles(getUserRoles())
                .build();

        User createdUser = userRepository.save(user);
        log.info("registerUser() --> Created a new user successfully: {}", createdUser.getUsername());
        return createdUser;
    }

    private Set<Role> getUserRoles() {
        Role role = roleRepository.findByRoleName("User")
                .orElseThrow(() -> new RoleNotFoundException("Role 'User' not found"));
        return new HashSet<>(Collections.singletonList(role));
    }


    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public String login(SigninRequest signinRequest) {
        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(signinRequest.getUsername(), signinRequest.getPassword())
                );
        User user = userRepository
                .findByUsername(signinRequest.getUsername())
                .orElseThrow(() -> new GlobalException("Not found username: " + signinRequest.getUsername(), HttpStatus.NOT_FOUND));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenUtil.generateToken(authentication);
        log.info("login() --> User '{}' logged in successfully", signinRequest.getUsername());

        return token;
    }

    @Override
    public UserDto getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("getMe() --> Retrieved details for user: {}", username);

        return userMapper.toDto(user);
    }

    @Override
    public void initiatePasswordReset(String email) {
        User existingUser = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new GlobalException("Not found user with email: " + email, HttpStatus.NOT_FOUND));

        String token = UUID.randomUUID().toString();
        Date expiration = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));

        existingUser.setResetToken(token);
        existingUser.setResetTokenExpiration(expiration);
        userRepository.save(existingUser);
        log.info("initiatePasswordReset() --> Password reset token generated for user: {}", existingUser.getUsername());

        PasswordResetEvent event = PasswordResetEvent.builder()
                .username(existingUser.getUsername())
                .resetToken(existingUser.getResetToken())
                .email(existingUser.getEmail())
                .build();

        eventPublisherService.publishEvent(EventConstant.PASSWORD_RESET, event);
    }

    @Override
    public boolean resetPassword(String token, PasswordResetRequest passwordResetRequest) {
        User existingUser = userRepository.findByResetToken(token).orElseThrow(() -> new GlobalException("Invalid token!!!", HttpStatus.BAD_REQUEST));

        Date now = new Date();
        if (now.after(existingUser.getResetTokenExpiration())) {
            throw new GlobalException("Token is expired!!!", HttpStatus.BAD_REQUEST);
        }


        if (!passwordResetRequest.getNewPassword().equals(passwordResetRequest.getConfirmPassword())) {
            log.warn("resetPassword() --> Password and confirm password do not match for user: {}", existingUser.getUsername());
            return false;
        }

        existingUser.setPassword(passwordEncoder.encode(passwordResetRequest.getNewPassword()));
        existingUser.setResetToken(null);
        existingUser.setResetTokenExpiration(null);
        userRepository.save(existingUser);
        log.info("resetPassword() --> Password reset successfully for user: {}", existingUser.getUsername());
        return true;
    }
}
