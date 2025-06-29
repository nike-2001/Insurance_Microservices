package com.nikhilspring.ClaimService.external.client;

import com.nikhilspring.ClaimService.config.FeignConfig;
import com.nikhilspring.ClaimService.external.response.PolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "POLICY-SERVICE", configuration = FeignConfig.class)
public interface PolicyService {

    @GetMapping("/policy/{policyId}")
    ResponseEntity<PolicyResponse> getPolicyById(@PathVariable long policyId);

    @GetMapping("/policy/{policyId}/validate")
    ResponseEntity<Void> validatePolicy(@PathVariable long policyId);
} 