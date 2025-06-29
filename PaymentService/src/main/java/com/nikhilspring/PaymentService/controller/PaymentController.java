package com.nikhilspring.PaymentService.controller;

import com.nikhilspring.PaymentService.model.PaymentRequest;
import com.nikhilspring.PaymentService.model.PaymentResponse;
import com.nikhilspring.PaymentService.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> processPayment(@RequestBody PaymentRequest paymentRequest) {
        return new ResponseEntity<>(
                paymentService.processPayment(paymentRequest),
                HttpStatus.OK
        );
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<PaymentResponse> getPaymentDetailsByPolicyId(@PathVariable String policyId) {
        return new ResponseEntity<>(
                paymentService.getPaymentDetailsByPolicyId(policyId),
                HttpStatus.OK
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<PaymentResponse> getPaymentDetailsByCustomerId(@PathVariable String customerId) {
        return new ResponseEntity<>(
                paymentService.getPaymentDetailsByCustomerId(customerId),
                HttpStatus.OK
        );
    }

}