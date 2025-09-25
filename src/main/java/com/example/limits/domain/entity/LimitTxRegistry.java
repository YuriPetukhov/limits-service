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
        name = "limit_tx_registry",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_tx_registry_user_scope_period_tx",
                        columnNames = {"user_id", "scope_key", "period_start", "tx_id"}
                )
        },
        indexes = {
                @Index(name = "idx_tx_registry_user_tx", columnList = "user_id, tx_id"),
                @Index(name = "idx_tx_registry_period",  columnList = "period_start")
        }
)
public class LimitTxRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "user_id",  nullable = false, length = 128)
    @EqualsAndHashCode.Include @ToString.Include
    private String userId;

    @Column(name = "scope_key", nullable = false, length = 128)
    @EqualsAndHashCode.Include @ToString.Include
    private String scopeKey;

    @Column(name = "period_start", nullable = false)
    @EqualsAndHashCode.Include @ToString.Include
    private Instant periodStart;

    @Column(name = "tx_id", nullable = false, length = 128)
    @EqualsAndHashCode.Include @ToString.Include
    private String txId;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "user_id",  referencedColumnName = "user_id",  insertable = false, updatable = false),
            @JoinColumn(name = "scope_key", referencedColumnName = "scope_key", insertable = false, updatable = false)
    })
    @ToString.Exclude
    private LimitBucket bucket;
}
