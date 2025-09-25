package com.example.limits.application.service.handler.cancel;

import com.example.limits.web.dto.CancelResponse;

/**
 * Результат обработки cancel-конвейера: создан ли ресурс, его id и полезная нагрузка.
 */
public record CancelOutcome(boolean created, String resourceId, CancelResponse response) {}
