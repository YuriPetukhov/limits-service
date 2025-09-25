package com.example.limits.domain.repository;

import com.example.limits.domain.entity.LimitBucket;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LimitBucketRepository extends JpaRepository<LimitBucket, Long> {

    Optional<LimitBucket> findByUserIdAndScopeKey(String userId, String scopeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")) // опционально
    @Query("select b from LimitBucket b where b.userId = :userId and b.scopeKey = :scopeKey")
    Optional<LimitBucket> findByUserIdAndScopeKeyForUpdate(@Param("userId") String userId,
                                                           @Param("scopeKey") String scopeKey);

    List<LimitBucket> findAllByUserIdAndScopeKeyIn(String userId, Collection<String> scopes);

    List<LimitBucket> findAllByUserId(String userId);

    @Query(value = """
        SELECT * 
          FROM limit_bucket
         WHERE next_reset_at <= :now
         ORDER BY next_reset_at
         FOR UPDATE SKIP LOCKED
         LIMIT :batch
        """, nativeQuery = true)
    List<LimitBucket> findExpiredForUpdate(@Param("now") Instant now,
                                           @Param("batch") int batch);
}