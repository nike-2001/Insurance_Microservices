package com.nikhilspring.PaymentService.service;

import com.nikhilspring.PaymentService.model.PaymentRequest;
import com.nikhilspring.PaymentService.model.PaymentResponse;

public interface PaymentService {
    long processPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByPolicyId(String policyId);

    PaymentResponse getPaymentDetailsByCustomerId(String customerId);
}