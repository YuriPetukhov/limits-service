package com.example.limits.domain.exception;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) { super(message); }
}
