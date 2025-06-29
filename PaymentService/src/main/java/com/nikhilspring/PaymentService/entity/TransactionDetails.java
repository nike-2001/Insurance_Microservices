package com.nikhilspring.PaymentService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "PAYMENT_DETAILS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "POLICY_ID")
    private long policyId;

    @Column(name = "CUSTOMER_ID")
    private String customerId;

    @Column(name = "POLICY_NUMBER")
    private String policyNumber;

    @Column(name = "PAYMENT_MODE")
    private String paymentMode;

    @Column(name = "PAYMENT_TYPE")
    private String paymentType;

    @Column(name = "REFERENCE_NUMBER")
    private String referenceNumber;

    @Column(name = "TRANSACTION_ID")
    private String transactionId;

    @Column(name = "PAYMENT_DATE")
    private Instant paymentDate;

    @Column(name = "STATUS")
    private String paymentStatus;

    @Column(name = "AMOUNT")
    private long amount;

    @Column(name = "DESCRIPTION")
    private String description;
}