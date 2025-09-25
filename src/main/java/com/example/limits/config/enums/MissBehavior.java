package com.example.limits.config.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Политика при отсутствии подходящей стратегии")
public enum MissBehavior {
    USE_DEFAULT,
    REJECT
}
