package com.windev.comment_service.dto.response;

import lombok.Data;

@Data
public class ReactionDto {
    private Long userId;
    private Long articleId;
    private Long commentId;
    private ReactionTypeDto reactionType;
}
