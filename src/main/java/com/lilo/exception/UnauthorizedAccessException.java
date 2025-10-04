package com.lilo.exception;

import lombok.Getter;

@Getter
public class UnauthorizedAccessException extends RuntimeException {
    private String apiMessage;
    public UnauthorizedAccessException(String message, String apiMessage) {
        super(message);
        this.apiMessage = apiMessage;
    }
    public  UnauthorizedAccessException(String apiMessage) {
    super(apiMessage);
    this.apiMessage = apiMessage;
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}