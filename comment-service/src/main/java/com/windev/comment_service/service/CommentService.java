package com.windev.comment_service.service;

import com.windev.comment_service.dto.request.CreateCommentRequest;
import com.windev.comment_service.dto.request.UpdateCommentRequest;
import com.windev.comment_service.dto.response.CommentDto;
import com.windev.comment_service.payload.response.CommentWithUserResponse;
import com.windev.comment_service.payload.response.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentDto addComment(CreateCommentRequest request);
    CommentWithUserResponse updateComment(Long commentId, UpdateCommentRequest request);
    void deleteComment(Long commentId);
    PaginatedResponse<CommentWithUserResponse> getCommentsByArticle(Long articleId, Pageable pageable);
    PaginatedResponse<CommentWithUserResponse> getCommentsByUser(Pageable pageable);
}
