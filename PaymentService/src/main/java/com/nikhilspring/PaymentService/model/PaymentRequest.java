package com.nikhilspring.PaymentService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {

    private long policyId;
    private long amount;
    private String referenceNumber;
    private PaymentMode paymentMode;
    private String paymentType; // PREMIUM, RENEWAL, CLAIM_PAYMENT, ADMINISTRATIVE_FEE
    private String description;
    private String customerId;
    private String policyNumber;
}