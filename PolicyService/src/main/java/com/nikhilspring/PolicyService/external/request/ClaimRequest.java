package com.nikhilspring.PolicyService.external.request;

import com.nikhilspring.PolicyService.model.PaymentMode;
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
    private long amount;
    private String claimType;
    private String description;

}