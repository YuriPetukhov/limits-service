package com.example.limits.domain.repository;

import com.example.limits.domain.entity.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    Optional<Strategy> findByNameAndVersion(String name, Integer version);

    List<Strategy> findAllByEnabledTrueAndIsDefaultTrue();

    List<Strategy> findAllByIsDefaultTrueAndEnabledTrue();
    List<Strategy> findAllByEnabled(boolean enabled);

    List<Strategy> findAllByIsDefault(boolean isDefault);

    List<Strategy> findAllByEnabledAndIsDefault(boolean enabled, boolean isDefault);

    @Modifying
    @Query("update Strategy s set s.isDefault = false where s.isDefault = true and s.id <> :id")
    int clearOtherDefaults(@Param("id") Long id);

    @Modifying
    @Query("update Strategy s set s.isDefault = true where s.id = :id")
    int setDefaultById(@Param("id") Long id);


}
