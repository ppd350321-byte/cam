package com.canteen.repository;

import com.canteen.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    Optional<PaymentRecord> findByChannelTradeNo(String channelTradeNo);

    Optional<PaymentRecord> findByOrderId(Long orderId);
}
