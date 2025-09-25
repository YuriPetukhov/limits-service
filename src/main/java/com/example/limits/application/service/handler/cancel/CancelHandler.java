package com.example.limits.application.service.handler.cancel;

import com.example.limits.web.dto.CancelRequest;

public interface CancelHandler {
    CancelOutcome handle(CancelRequest req);
}
