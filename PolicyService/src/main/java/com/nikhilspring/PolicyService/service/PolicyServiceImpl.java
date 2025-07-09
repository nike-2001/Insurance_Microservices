package com.nikhilspring.PolicyService.service;

import com.nikhilspring.PolicyService.entity.Policy;
import com.nikhilspring.PolicyService.entity.Product;
import com.nikhilspring.PolicyService.exception.CustomException;
import com.nikhilspring.PolicyService.external.client.ClaimService;
import com.nikhilspring.PolicyService.external.client.PaymentService;
import com.nikhilspring.PolicyService.external.client.ProductService;
import com.nikhilspring.PolicyService.external.request.ClaimRequest;
import com.nikhilspring.PolicyService.external.request.PaymentRequest;
import com.nikhilspring.PolicyService.external.response.ClaimResponse;
import com.nikhilspring.PolicyService.external.response.PaymentResponse;
import com.nikhilspring.PolicyService.external.response.ProductResponse;
import com.nikhilspring.PolicyService.model.PolicyRequest;
import com.nikhilspring.PolicyService.model.PolicyResponse;
import com.nikhilspring.PolicyService.repository.PolicyRepository;
import com.nikhilspring.PolicyService.repository.ProductRepository;
import com.nikhilspring.PolicyService.validation.PolicyValidationUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@Log4j2
public class PolicyServiceImpl implements PolicyService{

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ClaimService claimService;

    @Override
    @CacheEvict(value = {"policies", "policy-products"}, allEntries = true)
    public long issuePolicy(PolicyRequest policyRequest) {
        log.info("Issuing Policy Request: {}", policyRequest);
        
        // Validate request
        PolicyValidationUtil.validatePolicyRequest(policyRequest);
        log.info("Policy request validation passed");
        
        // Check if product exists and is active using ProductService API
        log.info("Checking if product with ID {} exists and is active via ProductService", policyRequest.getProductId());
        
        try {
            // Call ProductService to validate product
            log.info("Calling ProductService.validateProduct for product ID: {}", policyRequest.getProductId());
            productService.validateProduct(policyRequest.getProductId());
            log.info("Product validation successful for ID: {}", policyRequest.getProductId());
            
            // Get product details from ProductService
            log.info("Calling ProductService.getProductById for product ID: {}", policyRequest.getProductId());
            ResponseEntity<ProductResponse> productResponse = productService.getProductById(policyRequest.getProductId());
            
            if (productResponse == null) {
                log.error("ProductService.getProductById returned null ResponseEntity for product ID: {}", policyRequest.getProductId());
                throw new CustomException(
                    "ProductService returned null response for product ID: " + policyRequest.getProductId(),
                    "PRODUCT_SERVICE_ERROR",
                    500
                );
            }
            
            ProductResponse product = productResponse.getBody();
            log.info("ProductService response status: {}, body: {}", productResponse.getStatusCode(), product);
            
            if (product == null) {
                log.error("Product not found with ID: {} - ProductService returned null body", policyRequest.getProductId());
                throw new CustomException(
                    "Product not found with ID: " + policyRequest.getProductId(),
                    "PRODUCT_NOT_FOUND",
                    400
                );
            }
            
            if (!product.isActive()) {
                log.error("Product is inactive. Product: {}, isActive: {}", product.getProductName(), product.isActive());
                throw new CustomException(
                    "Product is inactive with ID: " + policyRequest.getProductId(),
                    "PRODUCT_INACTIVE",
                    400
                );
            }
            
            log.info("Found active product: {} (ID: {}, Active: {})", product.getProductName(), product.getProductId(), product.isActive());
            
        } catch (CustomException e) {
            // Re-throw CustomException as is
            log.error("Custom exception during product validation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error validating product with ID {}: {}", policyRequest.getProductId(), e.getMessage(), e);
            throw new CustomException(
                "Error validating product: " + e.getMessage(),
                "PRODUCT_VALIDATION_ERROR",
                500
            );
        }
        
        // Create policy entity
        Policy policy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .customerId(policyRequest.getCustomerId())
                .productId(policyRequest.getProductId())
                .premiumAmount(policyRequest.getPremiumAmount())
                .coverageAmount(policyRequest.getCoverageAmount())
                .policyStartDate(policyRequest.getPolicyStartDate())
                .policyEndDate(policyRequest.getPolicyEndDate())
                .policyStatus("ACTIVE")
                .createdDate(Instant.now())
                .updatedDate(Instant.now())
                .build();
        
        // Save policy
        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy created successfully with ID: {}", savedPolicy.getId());
        
        return savedPolicy.getId();
    }

    @Override
    @Cacheable(value = "policies", key = "#policyId")
    public PolicyResponse getPolicyDetails(long policyId) {
        log.info("Get policy details for Policy Id : {}", policyId);

        // Validate policy ID using utility
        PolicyValidationUtil.validatePolicyId(policyId);

        Policy policy
                = policyRepository.findById(policyId)
                .orElseThrow(() -> new CustomException("Policy not found for the policy Id:" + policyId,
                        "NOT_FOUND",
                        404));

        // Create response with basic policy details
        PolicyResponse policyResponse = PolicyResponse.builder()
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .policyStatus(policy.getPolicyStatus())
                .premiumAmount(policy.getPremiumAmount())
                .coverageAmount(policy.getCoverageAmount())
                .policyStartDate(policy.getPolicyStartDate())
                .policyEndDate(policy.getPolicyEndDate())
                .build();

        // Get product details from ProductService API
        try {
            log.info("Calling ProductService.getProductById for product ID: {}", policy.getProductId());
            ResponseEntity<ProductResponse> productResponseEntity = productService.getProductById(policy.getProductId());
            
            if (productResponseEntity == null) {
                log.warn("ProductService.getProductById returned null ResponseEntity for product ID: {}", policy.getProductId());
                throw new Exception("ProductService returned null response");
            }
            
            ProductResponse product = productResponseEntity.getBody();
            log.info("ProductService response status: {}, body: {}", productResponseEntity.getStatusCode(), product);
            
            if (product != null) {
                PolicyResponse.ProductDetails productDetails = PolicyResponse.ProductDetails
                        .builder()
                        .productName(product.getProductName())
                        .productId(product.getProductId())
                        .productType(product.getProductType())
                        .coverageType(product.getCoverageType())
                        .build();
                policyResponse.setProductDetails(productDetails);
                log.info("Successfully set product details for product ID: {}", policy.getProductId());
            } else {
                log.warn("ProductService returned null product body for product ID: {}", policy.getProductId());
                // Set default product details if not found
                PolicyResponse.ProductDetails productDetails = PolicyResponse.ProductDetails
                        .builder()
                        .productName("Product-" + policy.getProductId())
                        .productId(policy.getProductId())
                        .build();
                policyResponse.setProductDetails(productDetails);
                log.info("Set default product details for product ID: {}", policy.getProductId());
            }
        } catch (Exception e) {
            log.warn("Could not fetch product details from ProductService for product ID {}: {}", policy.getProductId(), e.getMessage());
            // Set default product details
            PolicyResponse.ProductDetails productDetails = PolicyResponse.ProductDetails
                    .builder()
                    .productName("Product-" + policy.getProductId())
                    .productId(policy.getProductId())
                    .build();
            policyResponse.setProductDetails(productDetails);
            log.info("Set default product details due to ProductService error for product ID: {}", policy.getProductId());
        }

        // Set default payment details
        PolicyResponse.PaymentDetails paymentDetails = PolicyResponse.PaymentDetails
                .builder()
                .paymentId(0L)
                .paymentStatus("NOT_AVAILABLE")
                .paymentDate(Instant.now())
                .paymentMode(com.nikhilspring.PolicyService.model.PaymentMode.CASH)
                .build();
        policyResponse.setPaymentDetails(paymentDetails);

        // Set default claim details
        PolicyResponse.ClaimDetails claimDetails = PolicyResponse.ClaimDetails
                .builder()
                .claimId(0L)
                .claimStatus("NOT_AVAILABLE")
                .claimDate(Instant.now())
                .claimType("NONE")
                .build();
        policyResponse.setClaimDetails(claimDetails);

        return policyResponse;
    }

    private String generatePolicyNumber() {
        return "POL-" + System.currentTimeMillis();
    }

    private String generateReferenceNumber() {
        return "REF-" + System.currentTimeMillis();
    }
} 