package com.windev.comment_service.dto.response;

import lombok.Data;

@Data
public class ReactionTypeDto {
    private Long id;

    private String type;

    private String description;
}
