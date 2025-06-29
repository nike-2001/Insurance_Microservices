package com.nikhilspring.PolicyService.controller;

import com.nikhilspring.PolicyService.entity.Product;
import com.nikhilspring.PolicyService.external.client.ProductService;
import com.nikhilspring.PolicyService.external.response.ProductResponse;
import com.nikhilspring.PolicyService.model.PolicyRequest;
import com.nikhilspring.PolicyService.model.PolicyResponse;
import com.nikhilspring.PolicyService.repository.ProductRepository;
import com.nikhilspring.PolicyService.service.PolicyService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/policy")
@Log4j2
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasAnyRole('Customer')")
    @PostMapping("/issue")
    public ResponseEntity<Long> issuePolicy(@RequestBody PolicyRequest policyRequest) {
        long policyId = policyService.issuePolicy(policyRequest);
        log.info("Policy Id: {}", policyId);
        return new ResponseEntity<>(policyId, HttpStatus.OK);
    }


    @PreAuthorize("hasAnyRole('Admin', 'Customer')")
    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyResponse> getPolicyDetails(@PathVariable long policyId) {
        PolicyResponse policyResponse = policyService.getPolicyDetails(policyId);
        return new ResponseEntity<>(policyResponse, HttpStatus.OK);
    }

    // Test endpoint without authentication for debugging
    @GetMapping("/{policyId}/test")
    public ResponseEntity<PolicyResponse> getPolicyDetailsTest(@PathVariable long policyId) {
        try {
            log.info("Test Get Policy Details for ID: {}", policyId);
            PolicyResponse policyResponse = policyService.getPolicyDetails(policyId);
            return new ResponseEntity<>(policyResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error in test get endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Test endpoint to check product status
    @GetMapping("/product/{productId}/status")
    public ResponseEntity<String> checkProductStatus(@PathVariable long productId) {
        try {
            log.info("Checking product status for ID: {} via ProductService", productId);
            
            // Call ProductService to get product details
            ResponseEntity<ProductResponse> productResponse = productService.getProductById(productId);
            ProductResponse product = productResponse.getBody();
            
            if (product == null) {
                return new ResponseEntity<>("Product not found with ID: " + productId, HttpStatus.NOT_FOUND);
            }
            
            String status = String.format("Product found: ID=%d, Name=%s, Active=%s", 
                product.getProductId(), product.getProductName(), product.isActive());
            
            log.info("Product status: {}", status);
            return new ResponseEntity<>(status, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error checking product status: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Test endpoint to validate product connection
    @GetMapping("/product/{productId}/validate")
    public ResponseEntity<String> validateProductConnection(@PathVariable long productId) {
        try {
            log.info("Validating product connection for ID: {} via ProductService", productId);
            
            // Call ProductService to validate product
            ResponseEntity<Void> validationResponse = productService.validateProduct(productId);
            
            if (validationResponse.getStatusCode().is2xxSuccessful()) {
                String message = "Product validation successful for ID: " + productId;
                log.info(message);
                return new ResponseEntity<>(message, HttpStatus.OK);
            } else {
                String message = "Product validation failed for ID: " + productId;
                log.error(message);
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            log.error("Error validating product connection: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("Policy Service is running", HttpStatus.OK);
    }
}