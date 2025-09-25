package com.example.limits.domain.repository;

import com.example.limits.domain.entity.Strategy;
import com.example.limits.domain.entity.UserStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserStrategyRepository extends JpaRepository<UserStrategy, Long> {
    @Query("""
        select us.strategy
          from UserStrategy us
          join us.strategy s
         where us.userId = :userId
           and us.active = true
           and s.enabled = true
           and (us.effectiveFrom is null or us.effectiveFrom <= :now)
           and (us.effectiveTo   is null or us.effectiveTo   >  :now)
        """)
    List<Strategy> findActiveStrategies(@Param("userId") String userId,
                                        @Param("now") Instant now);

    @Query(
            value = """
            select *
            from user_strategy us
            where us.user_id = :userId
              and us.is_active = true
              and (us.effective_from is null or us.effective_from <= :now)
              and (us.effective_to   is null or :now < us.effective_to)
            order by us.created_at desc
            limit 1
            """,
            nativeQuery = true
    )
    Optional<UserStrategy> findActiveByUserId(String userId, Instant now);

    Optional<UserStrategy> findByUserIdAndStrategyId(String userId, Long strategyId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update UserStrategy us
              set us.active = false
            where us.userId = :userId
              and us.active = true
           """)
    int deactivateAllActiveForUser(@Param("userId") String userId);
}
