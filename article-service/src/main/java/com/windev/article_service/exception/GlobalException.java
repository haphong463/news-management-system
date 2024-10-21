package com.windev.article_service.exception;

import org.springframework.http.HttpStatus;

public class GlobalException extends RuntimeException{
    private HttpStatus status;

    public GlobalException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
