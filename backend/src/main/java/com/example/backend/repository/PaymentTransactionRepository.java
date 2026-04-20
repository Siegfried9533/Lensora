package com.example.backend.repository;

import com.example.backend.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    Optional<PaymentTransaction> findByTransactionRef(String transactionRef);
    Optional<PaymentTransaction> findByOrderCode(String orderCode);
    Optional<PaymentTransaction> findTopByOrderCodeOrderByCreatedAtDesc(String orderCode);
}
