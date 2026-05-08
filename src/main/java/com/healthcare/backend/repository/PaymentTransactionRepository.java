package com.healthcare.backend.repository;

import com.healthcare.backend.entity.PaymentTransaction;
import com.healthcare.backend.entity.enums.PaymentTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findBySepayTransactionId(String sepayTransactionId);

    List<PaymentTransaction> findByPaymentRecord_PaymentRecordIdOrderByTransactionDateDesc(Long paymentRecordId);

    @Query("""
            select pt
            from PaymentTransaction pt
            join fetch pt.paymentRecord pr
            left join fetch pr.appointment appointment
            left join fetch pr.medicalRecord medicalRecord
            where pt.processStatus = :status
              and pt.transactionDate >= :fromDateTime
              and pt.transactionDate < :toDateTime
              and (:gateway is null or upper(pt.gateway) = upper(:gateway))
            order by pt.transactionDate asc
            """)
    List<PaymentTransaction> findSuccessfulRevenueTransactions(
            @Param("status") PaymentTransactionStatus status,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            @Param("gateway") String gateway
    );
}
