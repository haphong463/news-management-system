package com.windev.comment_service.payload.response;

import com.windev.comment_service.dto.response.CommentDto;
import com.windev.comment_service.dto.response.ReactionDto;
import com.windev.comment_service.dto.response.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentWithUserResponse {
    private Long id;

    private Long articleId;

    private Long userId;

    private String content;

    private Long parentCommentId;

    private List<CommentWithUserResponse> childComments;

    private Date createdAt;

    private Date updatedAt;

    private String username;

    private List<ReactionDto> reactions;
}
