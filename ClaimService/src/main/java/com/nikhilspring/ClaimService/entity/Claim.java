package com.nikhilspring.ClaimService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "CLAIMS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "CLAIM_NUMBER")
    private String claimNumber;

    @Column(name = "POLICY_ID")
    private long policyId;

    @Column(name = "CUSTOMER_ID")
    private String customerId;

    @Column(name = "CLAIM_TYPE")
    private String claimType;

    @Column(name = "CLAIM_AMOUNT")
    private long claimAmount;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CLAIM_STATUS")
    private String claimStatus;

    @Column(name = "CLAIM_DATE")
    private Instant claimDate;

    @Column(name = "PROCESSED_DATE")
    private Instant processedDate;

    @Column(name = "APPROVED_AMOUNT")
    private long approvedAmount;

    @Column(name = "REJECTION_REASON")
    private String rejectionReason;

    @Column(name = "CREATED_DATE")
    private Instant createdDate;

    @Column(name = "UPDATED_DATE")
    private Instant updatedDate;
} 