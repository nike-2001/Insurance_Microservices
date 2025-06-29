package com.nikhilspring.PaymentService.repository;

import com.nikhilspring.PaymentService.entity.TransactionDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionDetailsRepository extends JpaRepository<TransactionDetails, Long> {
    Optional<TransactionDetails> findByPolicyId(long policyId);
    Optional<TransactionDetails> findByCustomerId(String customerId);
}
