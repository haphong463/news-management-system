package com.windev.comment_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class GlobalException extends RuntimeException{
    private String message;
    private HttpStatus status;
}
