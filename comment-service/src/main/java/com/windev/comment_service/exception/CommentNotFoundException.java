package com.windev.comment_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommentNotFoundException extends RuntimeException{
    private String message;
}
