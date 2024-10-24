package com.windev.comment_service.service.impl;

import com.windev.comment_service.client.ReactionClient;
import com.windev.comment_service.client.UserClient;
import com.windev.comment_service.dto.request.CreateCommentRequest;
import com.windev.comment_service.dto.request.UpdateCommentRequest;
import com.windev.comment_service.dto.response.CommentDto;
import com.windev.comment_service.dto.response.ReactionDto;
import com.windev.comment_service.dto.response.UserDto;
import com.windev.comment_service.entity.Comment;
import com.windev.comment_service.exception.GlobalException;
import com.windev.comment_service.mapper.CommentMapper;
import com.windev.comment_service.payload.response.CommentWithUserResponse;
import com.windev.comment_service.payload.response.PaginatedResponse;
import com.windev.comment_service.repository.CommentRepository;
import com.windev.comment_service.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final ReactionClient reactionClient;

    @Override
    @Transactional
    public CommentDto addComment(CreateCommentRequest request) {
        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setUserId(request.getUserId());
        comment.setContent(request.getContent());

        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new GlobalException("Not found with ID: " + request.getParentCommentId(), HttpStatus.NOT_FOUND));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDtoWithChildren(savedComment);
    }

    @Override
    @Transactional
    public CommentWithUserResponse updateComment(Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new GlobalException("Not found with ID: " + commentId, HttpStatus.NOT_FOUND));

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        UserDto userDto = userClient.getUserById(comment.getUserId()).getBody();
        CommentWithUserResponse response = commentMapper.toDtoWithUser(updatedComment, userDto);
        response.setChildComments(commentMapper.mapChildCommentsWithUser(comment.getChildComments(), userDto));

        return response;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new GlobalException("Not found with ID: " + commentId, HttpStatus.NOT_FOUND));
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CommentWithUserResponse> getCommentsByArticle(Long articleId, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findByArticleIdAndParentCommentIsNull(articleId, pageable);
        return convertToPaginatedResponseDto(commentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CommentWithUserResponse> getCommentsByUser(Long userId, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findByUserId(userId, pageable);
        return convertToPaginatedResponseDto(commentPage);
    }

    private PaginatedResponse<CommentWithUserResponse> convertToPaginatedResponseDto(Page<Comment> commentPage) {

        List<Long> commentIds = commentPage.stream().flatMap(comment -> getAllCommentIds(comment).stream()).collect(Collectors.toList());

        List<ReactionDto> reactions = reactionClient.getReactionsByComments(commentIds).getBody();

        Map<Long, UserDto> userCache = new HashMap<>();
        List<CommentWithUserResponse> comments = commentPage.stream().map(comment -> mapCommentWithUser(comment, userCache)).collect(Collectors.toList());

        assignReactionsToComments(comments, reactions);

        return new PaginatedResponse<>(comments, commentPage.getNumber(), commentPage.getSize(), commentPage.getTotalPages(), commentPage.getTotalElements(), commentPage.isLast());
    }

    private CommentWithUserResponse mapCommentWithUser(Comment comment, Map<Long, UserDto> userCache) {

        UserDto userDto = userCache.computeIfAbsent(comment.getUserId(), id -> userClient.getUserById(id).getBody());

        CommentWithUserResponse response = commentMapper.toDtoWithUser(comment, userDto);
        response.setChildComments(comment.getChildComments().stream().map(child -> mapCommentWithUser(child, userCache)).collect(Collectors.toList()));

        return response;
    }

    private void assignReactionsToComments(List<CommentWithUserResponse> comments, List<ReactionDto> reactions) {

        Map<Long, List<ReactionDto>> reactionsMap = reactions.stream().collect(Collectors.groupingBy(ReactionDto::getCommentId));

        for (CommentWithUserResponse comment : comments) {
            comment.setReactions(reactionsMap.getOrDefault(comment.getId(), Collections.emptyList()));

            if (comment.getChildComments() != null && !comment.getChildComments().isEmpty()) {
                assignReactionsToComments(comment.getChildComments(), reactions);
            }
        }
    }

    private List<Long> getAllCommentIds(Comment comment) {
        List<Long> ids = new ArrayList<>();
        ids.add(comment.getId());

        if (comment.getChildComments() != null && !comment.getChildComments().isEmpty()) {
            for (Comment child : comment.getChildComments()) {
                ids.addAll(getAllCommentIds(child));
            }
        }

        return ids;
    }
}

