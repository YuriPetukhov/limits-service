package com.example.limits.application.service.handler.check;

import com.example.limits.web.dto.CheckLimitRequest;
import com.example.limits.web.dto.CheckLimitResponse;

public interface CheckHandler {
    CheckLimitResponse handle(CheckLimitRequest req);
}
