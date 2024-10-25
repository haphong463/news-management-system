package com.windev.user_service.service;

import com.windev.user_service.dto.request.PasswordResetRequest;
import com.windev.user_service.dto.response.UserDto;
import com.windev.user_service.entity.User;
import com.windev.user_service.dto.request.SigninRequest;
import com.windev.user_service.dto.request.SignupRequest;

import java.util.Optional;

public interface AuthService {
    UserDto registerUser(SignupRequest signupRequest);

    Optional<User> getUserByUsername(String username);

    String login(SigninRequest signinRequest);

    UserDto getMe();

    void initiatePasswordReset(String email);

    boolean resetPassword(String token, PasswordResetRequest passwordResetRequest);
}
