package com.example.limits.domain.exception;

public class LimitExceededException extends RuntimeException {
    public LimitExceededException(String message) { super(message); }
}
