package com.example.limits.application.service.handler.cancel;

public interface CancelStep {
    void execute(CancelContext context);

    default boolean supports(CancelContext context) {
        return true;
    }
}
