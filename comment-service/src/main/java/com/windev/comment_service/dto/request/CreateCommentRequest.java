package com.windev.comment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotNull(message = "User ID is required !!")
    private Long userId;

    @NotNull(message = "Article ID is required !!")
    private Long articleId;

    @NotBlank(message = "Content cannot be blank")
    private String content;

//    Optional
    private Long parentCommentId;
}
