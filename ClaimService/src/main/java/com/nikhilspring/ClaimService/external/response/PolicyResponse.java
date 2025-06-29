package com.nikhilspring.ClaimService.external.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolicyResponse {
    private long policyId;
    private String policyNumber;
    private String policyStatus;
    private long premiumAmount;
    private long coverageAmount;
    private Instant policyStartDate;
    private Instant policyEndDate;
    private ProductDetails productDetails;
    private PaymentDetails paymentDetails;
    private ClaimDetails claimDetails;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDetails {
        private String productName;
        private long productId;
        private String productType;
        private String coverageType;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PaymentDetails {
        private long paymentId;
        private String paymentMode;
        private String paymentStatus;
        private Instant paymentDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ClaimDetails {
        private long claimId;
        private String claimType;
        private String claimStatus;
        private Instant claimDate;
    }
} 