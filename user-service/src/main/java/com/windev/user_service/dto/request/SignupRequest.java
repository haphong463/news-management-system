package com.windev.user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "First name can not be blank!")
    @Size(min = 5, max = 50, message = "First name must be between 5 and 50 characters.")
    private String firstName;

    @NotBlank(message = "Last name can not be blank!")
    @Size(min = 5, max = 50, message = "Last name must be between 5 and 50 characters.")
    private String lastName;

    @NotBlank(message = "Username cannot be blank!")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters.")
    private String username;

    @NotBlank(message = "Email cannot be blank!")
    @Email(message = "Email address provided is not valid.")
    private String email;

    @NotBlank(message = "Password cannot be blank!")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,16}$",
            message = "Password must contain one digit from 1 to 9, one lowercase letter, one uppercase letter, one special character, no space, and it must be 8-16 characters long."
    )
    private String password;
}
