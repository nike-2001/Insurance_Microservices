package com.nikhilspring.PolicyService.external.response;

import com.nikhilspring.PolicyService.model.PaymentMode;
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
    private String status;
    private String claimType;
    private long amount;
    private Instant claimDate;
    private long policyId;
}