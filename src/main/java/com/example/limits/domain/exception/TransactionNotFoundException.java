package com.example.limits.domain.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) { super(message); }
}
