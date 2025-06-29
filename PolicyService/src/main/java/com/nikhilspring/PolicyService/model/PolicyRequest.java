package com.nikhilspring.PolicyService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolicyRequest {

    private long customerId;
    private long productId;
    private long premiumAmount;
    private long coverageAmount;
    private Instant policyStartDate;
    private Instant policyEndDate;
    private PaymentMode paymentMode;
}