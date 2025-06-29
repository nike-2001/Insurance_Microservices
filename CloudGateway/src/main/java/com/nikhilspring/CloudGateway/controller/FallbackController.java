package com.nikhilspring.CloudGateway.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Log4j2
public class FallbackController {

    @GetMapping("/policyServiceFallBack")
    public ResponseEntity<Map<String, Object>> policyServiceFallback() {
        log.error("Policy Service is down. Using fallback method.");
        
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("message", "Policy Service is currently unavailable");
        fallbackResponse.put("errorCode", "POLICY_SERVICE_UNAVAILABLE");
        fallbackResponse.put("timestamp", LocalDateTime.now().toString());
        fallbackResponse.put("status", "SERVICE_DOWN");
        
        return new ResponseEntity<>(fallbackResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping("/paymentServiceFallBack")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        log.error("Payment Service is down. Using fallback method.");
        
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("message", "Payment Service is currently unavailable");
        fallbackResponse.put("errorCode", "PAYMENT_SERVICE_UNAVAILABLE");
        fallbackResponse.put("timestamp", LocalDateTime.now().toString());
        fallbackResponse.put("status", "SERVICE_DOWN");
        
        return new ResponseEntity<>(fallbackResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping("/productServiceFallBack")
    public ResponseEntity<Map<String, Object>> productServiceFallback() {
        log.error("Product Service is down. Using fallback method.");
        
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("message", "Product Service is currently unavailable");
        fallbackResponse.put("errorCode", "PRODUCT_SERVICE_UNAVAILABLE");
        fallbackResponse.put("timestamp", LocalDateTime.now().toString());
        fallbackResponse.put("status", "SERVICE_DOWN");
        
        return new ResponseEntity<>(fallbackResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping("/claimServiceFallBack")
    public ResponseEntity<Map<String, Object>> claimServiceFallback() {
        log.error("Claim Service is down. Using fallback method.");
        
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("message", "Claim Service is currently unavailable");
        fallbackResponse.put("errorCode", "CLAIM_SERVICE_UNAVAILABLE");
        fallbackResponse.put("timestamp", LocalDateTime.now().toString());
        fallbackResponse.put("status", "SERVICE_DOWN");
        
        return new ResponseEntity<>(fallbackResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
}