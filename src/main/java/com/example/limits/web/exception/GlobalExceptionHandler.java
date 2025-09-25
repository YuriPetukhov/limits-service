package com.example.limits.web.exception;

import com.example.limits.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ProblemDetail> handleDuplicate(DuplicateTransactionException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://errors.example.com/duplicate-transaction"));
        pd.setTitle("Duplicate transaction");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTxNotFound(TransactionNotFoundException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("https://errors.example.com/tx-not-found"));
        pd.setTitle("Transaction not found");
        return ResponseEntity.unprocessableEntity().body(pd);
    }

    @ExceptionHandler(ValidationConflictException.class)
    public ResponseEntity<ProblemDetail> handleValidationConflict(ValidationConflictException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("https://errors.example.com/validation-conflict"));
        pd.setTitle("Validation conflict");
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleBeanValidation(MethodArgumentNotValidException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setType(URI.create("https://errors.example.com/validation"));
        pd.setTitle("Validation error");
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(StrategyNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleStrategyNotFound(StrategyNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://errors.example.com/strategy-not-found"));
        pd.setTitle("Strategy not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(UserStrategyNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserStrategyNotFound(UserStrategyNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://errors.example.com/user-strategy-not-found"));
        pd.setTitle("User strategy not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleLimitExceeded(LimitExceededException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("https://errors.example.com/limit-exceeded"));
        pd.setTitle("Insufficient limit");
        return ResponseEntity.unprocessableEntity().body(pd);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAny(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error on {} {}", req.getMethod(), req.getRequestURI(), ex);
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setType(URI.create("https://errors.example.com/internal"));
        pd.setTitle("Internal error");
        // на dev можно показать тип/сообщение
        pd.setDetail(ex.getClass().getSimpleName() + ": " + String.valueOf(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}