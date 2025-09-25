package com.example.limits.application.service.handler.check.impl;

import com.example.limits.web.dto.CheckLimitRequest;
import com.example.limits.web.dto.CheckLimitResponse;
import com.example.limits.application.service.handler.check.CheckContext;
import com.example.limits.application.service.handler.check.CheckHandler;
import com.example.limits.application.service.handler.check.CheckStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CheckHandlerImpl implements CheckHandler {
    private final List<CheckStep> steps;

    @Override
    public CheckLimitResponse handle(CheckLimitRequest req) {
        CheckContext ctx = new CheckContext(req);
        for (CheckStep s : steps) {
            s.execute(ctx);
            if (ctx.isStopped()) break;
        }
        return ctx.getResponse();
    }
}