package com.nikhilspring.PolicyService.external.client;

import com.nikhilspring.PolicyService.exception.CustomException;
import com.nikhilspring.PolicyService.external.request.ClaimRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "CLAIM-SERVICE/claim")
public interface ClaimService {

    @PostMapping
    public ResponseEntity<Long> processClaim(@RequestBody ClaimRequest claimRequest);

    default ResponseEntity<Long> fallback(Exception e) {
        throw new CustomException("Claim Service is not available",
                "UNAVAILABLE",
                500);
    }
}