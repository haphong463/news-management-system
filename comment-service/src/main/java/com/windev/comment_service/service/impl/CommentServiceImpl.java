package com.windev.comment_service.service.impl;

import com.windev.comment_service.dto.request.CreateCommentRequest;
import com.windev.comment_service.dto.request.UpdateCommentRequest;
import com.windev.comment_service.dto.response.CommentDto;
import com.windev.comment_service.entity.Comment;
import com.windev.comment_service.mapper.CommentMapper;
import com.windev.comment_service.repository.CommentRepository;
import com.windev.comment_service.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentDto addComment(CreateCommentRequest request) {
        Comment comment = new Comment();

        comment.setArticleId(request.getArticleId());
        comment.setUserId(request.getUserId());
        comment.setContent(request.getContent());

        if(request.getParentCommentId() != null){
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + request.getParentCommentId()));

            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDtoWithChildren(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        comment.setContent(request.getContent());

        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDtoWithChildren(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        commentRepository.delete(comment);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> getCommentsByArticle(Long articleId, Pageable pageable) {
        return commentRepository.findByArticleIdAndParentCommentIsNull(articleId, pageable).map(commentMapper::toDtoWithChildren);
    }

    @Override
    public Page<CommentDto> getCommentsByUser(Long userId, Pageable pageable) {
        return null;
    }
}
