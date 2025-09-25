package com.example.limits.domain.repository;

import com.example.limits.domain.entity.LimitTxRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface LimitTxRegistryRepository extends JpaRepository<LimitTxRegistry, Long> {

    @Query("""
           select r.amount
             from LimitTxRegistry r
            where r.userId = :userId and r.txId = :txId
         order by r.createdAt asc
           """)
    Optional<BigDecimal> findFirstAmountByUserAndTx(@Param("userId") String userId,
                                                    @Param("txId") String txId);

    Optional<LimitTxRegistry> findByUserIdAndTxId(String userId, String txId);
}
