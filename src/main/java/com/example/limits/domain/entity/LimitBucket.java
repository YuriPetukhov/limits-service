package com.example.limits.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "limit_bucket",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_limit_bucket_user_scope", columnNames = {"user_id", "scope_key"})
        },
        indexes = {
                @Index(name = "idx_limit_bucket_next_reset", columnList = "next_reset_at"),
                @Index(name = "idx_limit_bucket_user",       columnList = "user_id")
        }
)
public class LimitBucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 128)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String userId;

    @Column(name = "scope_key", nullable = false, length = 128)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String scopeKey;

    @Column(name = "base_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseLimit;

    @Column(name = "remaining", nullable = false, precision = 18, scale = 2)
    @ToString.Include
    private BigDecimal remaining;

    @Column(name = "interval_seconds", nullable = false)
    private Long intervalSeconds;

    @Column(name = "last_period_start", nullable = false)
    private Instant lastPeriodStart;

    @Column(name = "next_reset_at", nullable = false)
    private Instant nextResetAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;
}
