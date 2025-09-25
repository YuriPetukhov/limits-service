package com.example.limits.application.service.handler.debit.impl;

import com.example.limits.application.service.handler.debit.DebitContext;
import com.example.limits.application.service.handler.debit.DebitOutcome;
import com.example.limits.application.service.handler.debit.DebitStep;
import com.example.limits.web.dto.TransactionRequest;
import com.example.limits.application.service.handler.debit.DebitHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DebitHandlerImpl implements DebitHandler {
    private final List<DebitStep> steps;

    @Override
    public DebitOutcome handle(TransactionRequest req) {
        DebitContext ctx = new DebitContext(req);
        for (DebitStep step : steps) {
            step.execute(ctx);
            if (ctx.isStopped()) break;
        }
        String resourceId = (ctx.getResourceId() != null) ? ctx.getResourceId() : req.txId();
        return new DebitOutcome(ctx.isCreated(), resourceId, ctx.getResponse());
    }
}
