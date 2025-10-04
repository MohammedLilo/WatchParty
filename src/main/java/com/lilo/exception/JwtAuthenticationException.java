package com.lilo.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
@Getter
public class JwtAuthenticationException extends AuthenticationException {
    private String apiMessage;
    public JwtAuthenticationException(String message, String apiMessage) {
        super(message);
        this.apiMessage = apiMessage;
    }
    public JwtAuthenticationException(String apiMessage) {
        super(apiMessage);
    }
}
