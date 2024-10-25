package com.windev.user_service.service.impl;

import com.windev.user_service.config.CustomUserDetails;
import com.windev.user_service.dto.request.PasswordResetRequest;
import com.windev.user_service.dto.response.UserDto;
import com.windev.user_service.entity.Role;
import com.windev.user_service.entity.User;
import com.windev.user_service.exception.GlobalException;
import com.windev.user_service.mapper.UserMapper;
import com.windev.user_service.dto.request.SigninRequest;
import com.windev.user_service.dto.request.SignupRequest;
import com.windev.user_service.repository.RoleRepository;
import com.windev.user_service.repository.UserRepository;
import com.windev.user_service.service.AuthService;
import com.windev.user_service.util.EmailUtil;
import com.windev.user_service.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final EmailUtil emailUtil;


    @Override
    public UserDto registerUser(SignupRequest signupRequest) {
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        Role role = roleRepository.findByRoleName("User").orElseThrow(() -> {
            log.warn("registerUser() --> Role not found");
            return new RuntimeException("Role not found");
        });

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        user.setRoles(roles);

        User createdUser = userRepository.save(user);
        log.info("registerUser() --> Create a new user successfully!");


        return userMapper.toDto(createdUser);
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

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();


        return jwtTokenUtil.generateToken(authentication);
    }

    @Override
    public UserDto getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Lấy username từ authentication

        // Giả sử chúng ta có một phương thức để lấy user dựa trên username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toDto(user);
    }

    @Override
    public void initiatePasswordReset(String email) {
        User existingUser = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new GlobalException("Not found user with email: " + email, HttpStatus.NOT_FOUND));



        String token = UUID.randomUUID().toString();
        existingUser.setResetToken(token);
        existingUser.setResetTokenExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
        userRepository.save(existingUser);

        String resetLink = "http://localhost:8080/api/v1/auth/reset-password?token=" + token;
        emailUtil.sendMail(existingUser.getEmail(), "Password Reset Request", "Click the link to reset your password: " + resetLink);
    }

    @Override
    public boolean resetPassword(String token, PasswordResetRequest passwordResetRequest) {
        User existingUser = userRepository.findByResetToken(token).orElseThrow(() -> new GlobalException("Invalid token!!!", HttpStatus.BAD_REQUEST));

        if(new Date().after(existingUser.getResetTokenExpiration())){
            throw new GlobalException("Token is expired!!!",HttpStatus.BAD_REQUEST);
        }

        if(existingUser.getResetTokenExpiration().after(new Date())){
            if(!passwordResetRequest.getNewPassword().equals(passwordResetRequest.getConfirmPassword())){
                return false;
            }

            existingUser.setPassword(passwordEncoder.encode(passwordResetRequest.getNewPassword()));
            existingUser.setResetToken(null);
            existingUser.setResetTokenExpiration(null);

            userRepository.save(existingUser);
            return true;
        }

        return false;
    }


}
