package com.example.limits.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "strategy",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_strategy_name_version", columnNames = {"name", "version"})
        },
        indexes = {
                @Index(name = "idx_strategy_enabled", columnList = "enabled")
        }
)
public class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 100)
    @EqualsAndHashCode.Include @ToString.Include
    private String name;

    @Column(nullable = false)
    @EqualsAndHashCode.Include @ToString.Include
    private Integer version;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @ToString.Exclude
    @Column(name="dsl_text")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String dslText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spec_json", columnDefinition = "jsonb")
    @ToString.Exclude
    private String specJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "limits_json", columnDefinition = "jsonb")
    @ToString.Exclude
    private String limitsJson;

    private String checksum;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;
}
