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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final ReactionClient reactionClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public CommentDto addComment(CreateCommentRequest request) {
        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());

        UserDto user = getCurrentUser();

        comment.setUserId(user.getId());
        comment.setContent(request.getContent());

        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new GlobalException("Not found with ID: " + request.getParentCommentId(), HttpStatus.NOT_FOUND));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // Xóa cache liên quan
        evictCommentsCacheByArticle(request.getArticleId());

        return commentMapper.toDtoWithChildren(savedComment);
    }

    @Override
    @Transactional
    public CommentWithUserResponse updateComment(Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException("Not found with ID: " + commentId, HttpStatus.NOT_FOUND));

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        UserDto userDto = getUserById(comment.getUserId());
        CommentWithUserResponse response = commentMapper.toDtoWithUser(updatedComment, userDto);
        response.setChildComments(commentMapper.mapChildCommentsWithUser(comment.getChildComments(), userDto));

        // Xóa cache liên quan
        evictCommentsCacheByArticle(updatedComment.getArticleId());

        return response;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException("Not found with ID: " + commentId, HttpStatus.NOT_FOUND));

        List<Long> commentIds = getAllCommentIds(comment);

        commentRepository.delete(comment);



        // Xóa cache liên quan
        for (Long id : commentIds) {
            String reactionKey = "reactions:" + id;
            redisTemplate.delete(reactionKey);
        }

        evictCommentsCacheByArticle(comment.getArticleId());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CommentWithUserResponse> getCommentsByArticle(Long articleId, Pageable pageable) {
        String key = "commentsByArticle:" + articleId + ":" + pageable.getPageNumber();
        PaginatedResponse<CommentWithUserResponse> response = (PaginatedResponse<CommentWithUserResponse>) redisTemplate.opsForValue().get(key);

        if (response == null) {
            response = fetchCommentsByArticle(articleId, pageable);
            redisTemplate.opsForValue().set(key, response, Duration.ofMinutes(60));
        }

        return response;
    }

    private PaginatedResponse<CommentWithUserResponse> fetchCommentsByArticle(Long articleId, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findByArticleIdAndParentCommentIsNull(articleId, pageable);
        return convertToPaginatedResponseDto(commentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CommentWithUserResponse> getCommentsByUser(Pageable pageable) {
        UserDto userDto = getCurrentUser();

        Page<Comment> commentPage = commentRepository.findByUserId(userDto.getId(), pageable);
        return convertToPaginatedResponseDto(commentPage);
    }

    private PaginatedResponse<CommentWithUserResponse> convertToPaginatedResponseDto(Page<Comment> commentPage) {

        List<Long> commentIds = commentPage.stream()
                .flatMap(comment -> getAllCommentIds(comment).stream())
                .collect(Collectors.toList());

        List<ReactionDto> reactions = getReactionsByComments(commentIds);

        Map<Long, UserDto> userCache = new HashMap<>();
        List<CommentWithUserResponse> comments = commentPage.stream()
                .map(comment -> mapCommentWithUser(comment, userCache))
                .collect(Collectors.toList());

        assignReactionsToComments(comments, reactions);

        return new PaginatedResponse<>(
                comments,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalPages(),
                commentPage.getTotalElements(),
                commentPage.isLast()
        );
    }

    private CommentWithUserResponse mapCommentWithUser(Comment comment, Map<Long, UserDto> userCache) {

        UserDto userDto = userCache.computeIfAbsent(comment.getUserId(), id -> getUserById(id));

        CommentWithUserResponse response = commentMapper.toDtoWithUser(comment, userDto);
        response.setChildComments(
                comment.getChildComments().stream()
                        .map(child -> mapCommentWithUser(child, userCache))
                        .collect(Collectors.toList())
        );

        return response;
    }

    private void assignReactionsToComments(List<CommentWithUserResponse> comments, List<ReactionDto> reactions) {

        Map<Long, List<ReactionDto>> reactionsMap = reactions.stream()
                .collect(Collectors.groupingBy(ReactionDto::getCommentId));

        for (CommentWithUserResponse comment : comments) {
            comment.setReactions(reactionsMap.getOrDefault(comment.getId(), Collections.emptyList()));

            if (comment.getChildComments() != null && !comment.getChildComments().isEmpty()) {
                assignReactionsToComments(comment.getChildComments(), reactions);
            }
        }
    }

    private List<Long> getAllCommentIds(Comment rootComment) {
        List<Long> ids = new ArrayList<>();
        Queue<Comment> queue = new LinkedList<>();
        queue.add(rootComment);

        while (!queue.isEmpty()) {
            Comment current = queue.poll();
            ids.add(current.getId());

            if (current.getChildComments() != null && !current.getChildComments().isEmpty()) {
                queue.addAll(current.getChildComments());
            }
        }

        return ids;
    }


    private UserDto getCurrentUser() {
        return userClient.getCurrentUser().getBody();
    }

    private UserDto getUserById(Long userId) {
        String key = "user:" + userId;
        UserDto userDto = (UserDto) redisTemplate.opsForValue().get(key);

        if (userDto == null) {
            userDto = userClient.getUserById(userId).getBody();
            if (userDto != null) {
                redisTemplate.opsForValue().set(key, userDto, Duration.ofMinutes(60));
            }
        }

        return userDto;
    }

    private List<ReactionDto> getReactionsByComments(List<Long> commentIds) {
        List<ReactionDto> allReactions = new ArrayList<>();
        List<Long> missingCommentIds = new ArrayList<>();

        // Tạo danh sách các key cần lấy từ cache
        List<String> reactionKeys = commentIds.stream()
                .map(id -> "reactions:" + id)
                .collect(Collectors.toList());

        // Lấy tất cả các phản ứng từ cache một cách batch
        List<Object> cachedReactions = redisTemplate.opsForValue().multiGet(reactionKeys);

        for (int i = 0; i < commentIds.size(); i++) {
            Long commentId = commentIds.get(i);
            @SuppressWarnings("unchecked")
            List<ReactionDto> reactions = (List<ReactionDto>) cachedReactions.get(i);

            if (reactions == null) {
                missingCommentIds.add(commentId);
            } else {
                allReactions.addAll(reactions);
            }
        }

        // Nếu có phản ứng thiếu trong cache, lấy từ ReactionClient và cập nhật cache
        if (!missingCommentIds.isEmpty()) {
            List<ReactionDto> fetchedReactions = reactionClient.getReactionsByComments(missingCommentIds).getBody();
            if (fetchedReactions != null) {
                // Phân loại phản ứng theo commentId
                Map<Long, List<ReactionDto>> reactionsMap = fetchedReactions.stream()
                        .collect(Collectors.groupingBy(ReactionDto::getCommentId));

                // Cập nhật cache và thêm vào kết quả
                for (Long missingId : missingCommentIds) {
                    List<ReactionDto> reactionsForId = reactionsMap.getOrDefault(missingId, Collections.emptyList());
                    if (!reactionsForId.isEmpty()) {
                        redisTemplate.opsForValue().set("reactions:" + missingId, reactionsForId, Duration.ofMinutes(60));
                        allReactions.addAll(reactionsForId);
                    }
                }
            }
        }

        return allReactions;
    }



    private String generateReactionCacheKey(List<Long> commentIds) {
        List<Long> sortedIds = new ArrayList<>(commentIds);
        Collections.sort(sortedIds);
        return "reactions:" + sortedIds.toString();
    }

    private void evictCommentsCacheByArticle(Long articleId) {
        String pattern = "commentsByArticle:" + articleId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}

