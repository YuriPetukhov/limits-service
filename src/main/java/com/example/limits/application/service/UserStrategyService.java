package com.example.limits.application.service;

import com.example.limits.web.dto.AssignUserStrategyRequest;
import com.example.limits.web.dto.UserStrategyResponse;
import com.example.limits.application.result.OperationResult;

public interface UserStrategyService {
    OperationResult<UserStrategyResponse> assign(String userId, AssignUserStrategyRequest req);
    UserStrategyResponse getActive(String userId);
}