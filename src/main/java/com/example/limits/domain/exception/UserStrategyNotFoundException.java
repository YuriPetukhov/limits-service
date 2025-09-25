package com.example.limits.domain.exception;

public class UserStrategyNotFoundException extends RuntimeException {
    public UserStrategyNotFoundException(String message) {
        super(message);
    }
}
