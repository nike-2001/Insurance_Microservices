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
public class PaymentResponse {
    private long paymentId;
    private String status;
    private String paymentMode;
    private long amount;
    private Instant paymentDate;
    private long policyId;
    private String paymentType;
    private String referenceNumber;
    private String customerId;
    private String policyNumber;
    private String transactionId;
} 