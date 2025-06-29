package com.nikhilspring.ClaimService.external.client;

import com.nikhilspring.ClaimService.config.FeignConfig;
import com.nikhilspring.ClaimService.external.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PAYMENT-SERVICE", configuration = FeignConfig.class)
public interface PaymentService {

    @GetMapping("/payment/policy/{policyId}")
    ResponseEntity<PaymentResponse> getPaymentByPolicyId(@PathVariable long policyId);

    @GetMapping("/payment/policy/{policyId}/status")
    ResponseEntity<String> getPaymentStatus(@PathVariable long policyId);
} 