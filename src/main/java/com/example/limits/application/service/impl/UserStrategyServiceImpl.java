package com.example.limits.application.service.impl;

import com.example.limits.application.service.UserStrategyService;
import com.example.limits.application.service.handler.strategy.assign.AssignHandler;
import com.example.limits.application.service.handler.strategy.assign.AssignOutcome;
import com.example.limits.web.dto.AssignUserStrategyRequest;
import com.example.limits.web.dto.UserStrategyResponse;
import com.example.limits.domain.entity.UserStrategy;
import com.example.limits.domain.exception.UserStrategyNotFoundException;
import com.example.limits.web.mapper.UserStrategyResponseMapper;
import com.example.limits.domain.repository.UserStrategyRepository;
import com.example.limits.application.result.OperationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Привязки пользователя к стратегиям: назначение и чтение активной.
 */
@Service
@RequiredArgsConstructor
public class UserStrategyServiceImpl implements UserStrategyService {

    private final UserStrategyRepository userStrategyRepository;
    private final UserStrategyResponseMapper userStrategyResponseMapper;
    private final AssignHandler assignHandler;

    @Override
    @Transactional
    public OperationResult<UserStrategyResponse> assign(String userId, AssignUserStrategyRequest request) {
        AssignOutcome outcome = assignHandler.handle(userId, request);
        return outcome.created()
                ? OperationResult.created(outcome.resourceId(), outcome.response())
                : OperationResult.ok(outcome.resourceId(), outcome.response());
    }

    @Override
    @Transactional(readOnly = true)
    public UserStrategyResponse getActive(String userId) {
        Instant now = Instant.now();
        UserStrategy activeBinding = userStrategyRepository.findActiveByUserId(userId, now)
                .orElseThrow(() -> new UserStrategyNotFoundException("Active strategy not found for userId=" + userId));
        return userStrategyResponseMapper.toResponse(activeBinding);
    }
}
