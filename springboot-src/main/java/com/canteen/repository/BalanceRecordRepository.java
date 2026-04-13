package com.canteen.repository;

import com.canteen.entity.BalanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BalanceRecordRepository extends JpaRepository<BalanceRecord, Long> {

    List<BalanceRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COALESCE(SUM(ABS(b.amount)), 0) FROM BalanceRecord b WHERE b.userId = :userId AND b.type = 'vip_purchase'")
    BigDecimal sumVipPurchaseByUserId(@Param("userId") Long userId);
}
