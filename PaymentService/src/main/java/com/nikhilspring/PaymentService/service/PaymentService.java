package com.nikhilspring.PaymentService.service;

import com.nikhilspring.PaymentService.model.PaymentRequest;
import com.nikhilspring.PaymentService.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}