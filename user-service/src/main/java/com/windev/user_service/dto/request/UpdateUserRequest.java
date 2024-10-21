package com.windev.user_service.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
}
