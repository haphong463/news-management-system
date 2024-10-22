package com.windev.comment_service.dto.response;

import com.windev.comment_service.entity.Comment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentDto {
    private Long id;

    private Long articleId;

    private Long userId;

    private String content;

    private Long parentCommentId;

    private List<CommentDto> childComments;

    private Date createdAt;

    private Date updatedAt;
}
