package com.windev.notification_service.dto;

import lombok.Data;

@Data
public class ArticleDto {
    private UserDto user;
    private String content;
    // ... must be updated
}
