package com.windev.comment_service.service;

import com.windev.comment_service.dto.request.CreateCommentRequest;
import com.windev.comment_service.dto.request.UpdateCommentRequest;
import com.windev.comment_service.dto.response.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentDto addComment(CreateCommentRequest request);
    CommentDto updateComment(Long commentId, UpdateCommentRequest request);
    void deleteComment(Long commentId);
    Page<CommentDto> getCommentsByArticle(Long articleId, Pageable pageable);
    Page<CommentDto> getCommentsByUser(Long userId, Pageable pageable);
}
