package com.windev.comment_service.service.impl;

import com.windev.comment_service.client.ReactionClient;
import com.windev.comment_service.client.UserClient;
import com.windev.comment_service.dto.request.CreateCommentRequest;
import com.windev.comment_service.dto.request.UpdateCommentRequest;
import com.windev.comment_service.dto.response.CommentDto;
import com.windev.comment_service.dto.response.ReactionDto;
import com.windev.comment_service.dto.response.UserDto;
import com.windev.comment_service.entity.Comment;
import com.windev.comment_service.mapper.CommentMapper;
import com.windev.comment_service.payload.response.CommentWithUserResponse;
import com.windev.comment_service.payload.response.PaginatedResponse;
import com.windev.comment_service.repository.CommentRepository;
import com.windev.comment_service.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + request.getParentCommentId()));

            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDtoWithChildren(savedComment);
    }

    @Override
    @Transactional
    public CommentWithUserResponse updateComment(Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

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
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        commentRepository.delete(comment);

    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CommentWithUserResponse> getCommentsByArticle(Long articleId, Pageable pageable) {
        return convertToPaginatedResponseDto(commentRepository.findByArticleIdAndParentCommentIsNull(articleId,pageable));
    }

    @Override
    public PaginatedResponse<CommentWithUserResponse> getCommentsByUser(Long userId, Pageable pageable) {
        return convertToPaginatedResponseDto(commentRepository.findByUserId(userId, pageable));
    }

    private PaginatedResponse<CommentWithUserResponse> convertToPaginatedResponseDto(Page<Comment> commentPage) {
        List<CommentWithUserResponse> comments = commentPage.getContent().stream().map(comment -> {
            // Lấy thông tin user từ UserClient
            UserDto userDto = userClient.getUserById(comment.getUserId()).getBody();
            CommentWithUserResponse response = commentMapper.toDtoWithUser(comment, userDto);

            // Map child comments (bình luận con)
            response.setChildComments(commentMapper.mapChildCommentsWithUser(comment.getChildComments(), userDto));

            return response;
        }).collect(Collectors.toList());

        // Lấy tất cả commentId để gửi batch request đến ReactionClient
        List<Long> commentIds = commentPage.getContent().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // Gọi ReactionClient chỉ một lần với tất cả commentIds
        List<ReactionDto> reactionsResponse = reactionClient.getReactionsByComments(commentIds).getBody();

        // Ánh xạ các reactions vào từng comment
        comments.forEach(comment -> {
            List<ReactionDto> commentReactions = reactionsResponse.stream()
                    .filter(reaction -> reaction.getCommentId().equals(comment.getId()))
                    .collect(Collectors.toList());
            comment.setReactions(commentReactions); // Set phản ứng cho mỗi comment
        });

        return new PaginatedResponse<>(comments, commentPage.getNumber(), commentPage.getSize(),
                commentPage.getTotalPages(), commentPage.getTotalElements(), commentPage.isLast());
    }

}
