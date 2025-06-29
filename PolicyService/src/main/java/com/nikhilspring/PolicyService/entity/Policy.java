package com.nikhilspring.PolicyService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "POLICY_DETAILS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "POLICY_NUMBER")
    private String policyNumber;

    @Column(name = "CUSTOMER_ID")
    private long customerId;

    @Column(name = "PRODUCT_ID")
    private long productId;

    @Column(name = "PREMIUM_AMOUNT")
    private long premiumAmount;

    @Column(name = "COVERAGE_AMOUNT")
    private long coverageAmount;

    @Column(name = "POLICY_START_DATE")
    private Instant policyStartDate;

    @Column(name = "POLICY_END_DATE")
    private Instant policyEndDate;

    @Column(name = "STATUS")
    private String policyStatus;

    @Column(name = "CREATED_DATE")
    private Instant createdDate;

    @Column(name = "UPDATED_DATE")
    private Instant updatedDate;
}