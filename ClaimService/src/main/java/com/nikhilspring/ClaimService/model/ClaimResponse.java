package com.nikhilspring.ClaimService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClaimResponse {
    private long claimId;
    private String claimNumber;
    private long policyId;
    private String customerId;
    private String claimType;
    private long claimAmount;
    private String description;
    private String status;
    private Instant claimDate;
    private long approvedAmount;
    private String rejectionReason;
} 