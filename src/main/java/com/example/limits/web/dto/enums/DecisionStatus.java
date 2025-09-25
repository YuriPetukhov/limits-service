package com.example.limits.web.dto.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус решения по транзакции")
public enum DecisionStatus {
    @JsonProperty("APPROVED")
    APPROVED,
    @JsonProperty("DECLINED")
    DECLINED
}
