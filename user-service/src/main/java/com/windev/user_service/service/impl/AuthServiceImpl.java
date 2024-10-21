package com.windev.user_service.service.impl;

import com.windev.user_service.config.CustomUserDetails;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
}
