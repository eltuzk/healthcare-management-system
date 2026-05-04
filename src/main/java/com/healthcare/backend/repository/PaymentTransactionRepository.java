package com.healthcare.backend.repository;

import com.healthcare.backend.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findBySepayTransactionId(String sepayTransactionId);

    List<PaymentTransaction> findByPaymentRecord_PaymentRecordIdOrderByTransactionDateDesc(Long paymentRecordId);
}
