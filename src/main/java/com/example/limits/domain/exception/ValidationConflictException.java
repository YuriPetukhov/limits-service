package com.example.limits.domain.exception;

public class ValidationConflictException extends RuntimeException {
    public ValidationConflictException(String message) { super(message); }
}
