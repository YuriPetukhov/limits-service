package com.example.limits.application.service.handler.debit;

public interface DebitStep {
    boolean supports(DebitContext ctx);
    void execute(DebitContext ctx);
}
