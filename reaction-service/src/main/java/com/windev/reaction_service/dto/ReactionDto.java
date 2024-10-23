package com.windev.reaction_service.dto;

import lombok.Data;

@Data
public class ReactionDto {

    private Long userId;
    private Long articleId;
    private Long commentId;
    private Long reactionTypeId;
}
