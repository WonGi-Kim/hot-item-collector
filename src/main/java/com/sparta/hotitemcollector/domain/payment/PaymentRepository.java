package com.sparta.hotitemcollector.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMerchantUid(String merchatUid);

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByImpUid(String impUid);
}
