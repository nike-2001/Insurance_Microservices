package com.nikhilspring.PaymentService.external.client;

import com.nikhilspring.PaymentService.external.response.PolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "POLICY-SERVICE/policy")
public interface PolicyService {

    @GetMapping("/{policyId}")
    ResponseEntity<PolicyResponse> getPolicyById(
            @PathVariable("policyId") long policyId
    );
} 