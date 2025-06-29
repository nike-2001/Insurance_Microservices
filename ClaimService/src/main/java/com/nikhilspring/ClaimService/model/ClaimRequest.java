package com.nikhilspring.ClaimService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClaimRequest {
    private long policyId;
    private String customerId;
    private String claimType;
    private long claimAmount;
    private String description;
} 