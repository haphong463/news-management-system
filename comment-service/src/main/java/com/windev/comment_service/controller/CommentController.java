package com.windev.comment_service.controller;

import com.windev.comment_service.dto.request.CreateCommentRequest;
import com.windev.comment_service.dto.request.UpdateCommentRequest;
import com.windev.comment_service.dto.response.CommentDto;
import com.windev.comment_service.payload.response.CommentWithUserResponse;
import com.windev.comment_service.payload.response.PaginatedResponse;
import com.windev.comment_service.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> addComment(@Valid @RequestBody CreateCommentRequest request){
        try {
            CommentDto commentDto = commentService.addComment(request);
            return new ResponseEntity<>(commentDto, HttpStatus.CREATED);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<?> getCommentsByArticle(
            @PathVariable Long articleId,
            Pageable pageable) {
        try {
            PaginatedResponse<CommentWithUserResponse> comments = commentService.getCommentsByArticle(articleId, pageable);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getCommentsByUser(
            @PathVariable Long userId,
            Pageable pageable) {
        try {
            PaginatedResponse<CommentWithUserResponse> comments = commentService.getCommentsByUser(userId, pageable);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("{commentId}")
    public ResponseEntity<?> editComment(@PathVariable("commentId") Long commentId, @RequestBody UpdateCommentRequest updateCommentRequest){
        try {
            CommentWithUserResponse editedComment = commentService.updateComment(commentId, updateCommentRequest);
            return new ResponseEntity<>(editedComment, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") Long commentId){
        try {
            commentService.deleteComment(commentId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
