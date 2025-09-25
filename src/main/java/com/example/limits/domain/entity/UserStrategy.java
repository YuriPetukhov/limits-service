package com.example.limits.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "user_strategy",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_strategy_pair", columnNames = {"user_id", "strategy_id"})
        }
)
public class UserStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name="user_id", nullable = false, length = 128)
    @EqualsAndHashCode.Include @ToString.Include
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strategy_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_strategy_strategy"))
    @ToString.Exclude
    private Strategy strategy;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "effective_from")
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

}
