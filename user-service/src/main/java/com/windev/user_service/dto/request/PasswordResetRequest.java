package com.windev.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {

    @NotBlank(message = "New password cannot be blank!")
    private String newPassword;

    @NotBlank(message = "Confirm password cannot be blank!")
    private String confirmPassword;
}
