package com.nikhilspring.ClaimService.service;

import com.nikhilspring.ClaimService.entity.Claim;
import com.nikhilspring.ClaimService.exception.ClaimServiceCustomException;
import com.nikhilspring.ClaimService.model.ClaimRequest;
import com.nikhilspring.ClaimService.model.ClaimResponse;
import com.nikhilspring.ClaimService.repository.ClaimRepository;
import com.nikhilspring.ClaimService.validation.ClaimValidationUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class ClaimServiceImpl implements ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimValidationService claimValidationService;

    @Override
    @CacheEvict(value = {"claims", "claim-status"}, allEntries = true)
    public long fileClaim(ClaimRequest claimRequest) {
        log.info("Filing claim: {}", claimRequest);

        // Step 1: Basic validation using utility
        ClaimValidationUtil.validateClaimRequest(claimRequest);

        // Step 1.5: Basic policy existence check
        if (claimRequest.getPolicyId() <= 0) {
            log.error("Invalid policy ID provided: {}", claimRequest.getPolicyId());
            throw new ClaimServiceCustomException(
                "Invalid policy ID: " + claimRequest.getPolicyId(),
                "INVALID_POLICY_ID",
                400
            );
        }

        // Step 1.6: Basic customer ID validation
        if (claimRequest.getCustomerId() == null || claimRequest.getCustomerId().trim().isEmpty()) {
            log.error("Invalid customer ID provided: null or empty");
            throw new ClaimServiceCustomException(
                "Customer ID cannot be null or empty",
                "INVALID_CUSTOMER_ID",
                400
            );
        }

        if (!claimRequest.getCustomerId().matches("^[A-Z0-9]+$")) {
            log.error("Invalid customer ID format provided: {}", claimRequest.getCustomerId());
            throw new ClaimServiceCustomException(
                "Invalid customer ID format. Must contain only uppercase letters and numbers: " + claimRequest.getCustomerId(),
                "INVALID_CUSTOMER_ID_FORMAT",
                400
            );
        }

        // Step 2: Comprehensive validation (policy, customer, payment)
        try {
            log.info("Starting comprehensive validation for policy ID: {} and customer ID: {}", 
                claimRequest.getPolicyId(), claimRequest.getCustomerId());
            claimValidationService.validateClaimRequest(claimRequest);
            log.info("Comprehensive validation passed for policy ID: {} and customer ID: {}", 
                claimRequest.getPolicyId(), claimRequest.getCustomerId());
        } catch (ClaimServiceCustomException e) {
            log.error("Claim validation failed for policy ID {} and customer ID {}: {}", 
                claimRequest.getPolicyId(), claimRequest.getCustomerId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during claim validation for policy ID {} and customer ID {}: {}", 
                claimRequest.getPolicyId(), claimRequest.getCustomerId(), e.getMessage());
            throw new ClaimServiceCustomException(
                "Failed to validate claim: " + e.getMessage(),
                "CLAIM_VALIDATION_FAILED",
                500
            );
        }

        // Step 3: Check if claim already exists for this policy
        Claim existingClaim = claimRepository.findFirstByPolicyIdOrderByClaimDateDesc(claimRequest.getPolicyId());
        if (existingClaim != null) {
            log.error("Claim already exists for policy ID: {}", claimRequest.getPolicyId());
            throw new ClaimServiceCustomException(
                "Claim already exists for policy ID: " + claimRequest.getPolicyId(),
                "CLAIM_ALREADY_EXISTS",
                409
            );
        }

        // Step 4: Create and save the claim
        log.info("Creating claim for validated policy ID: {} and customer ID: {}", 
            claimRequest.getPolicyId(), claimRequest.getCustomerId());
        Claim claim = Claim.builder()
                .claimNumber(generateClaimNumber())
                .policyId(claimRequest.getPolicyId())
                .customerId(claimRequest.getCustomerId())
                .claimType(claimRequest.getClaimType())
                .claimAmount(claimRequest.getClaimAmount())
                .description(claimRequest.getDescription())
                .claimStatus("PENDING")
                .claimDate(Instant.now())
                .createdDate(Instant.now())
                .updatedDate(Instant.now())
                .approvedAmount(0)
                .build();

        try {
            claim = claimRepository.save(claim);
            log.info("Claim filed successfully with ID: {} for policy ID: {} and customer ID: {}", 
                claim.getId(), claimRequest.getPolicyId(), claimRequest.getCustomerId());
            return claim.getId();
        } catch (Exception e) {
            log.error("Error filing claim for policy ID {} and customer ID {}: {}", 
                claimRequest.getPolicyId(), claimRequest.getCustomerId(), e.getMessage());
            throw new ClaimServiceCustomException(
                "Failed to file claim: " + e.getMessage(),
                "CLAIM_FILING_FAILED",
                500
            );
        }
    }

    @Override
    @Cacheable(value = "claims", key = "'policy-' + #policyId")
    public ClaimResponse getClaimByPolicyId(long policyId) {
        log.info("Getting claim for policy ID: {}", policyId);

        // Validate policy ID using utility
        ClaimValidationUtil.validatePolicyId(policyId);

        Claim claim = claimRepository.findFirstByPolicyIdOrderByClaimDateDesc(policyId);
        if (claim == null) {
            throw new ClaimServiceCustomException(
                "Claim not found for policy ID: " + policyId,
                "CLAIM_NOT_FOUND",
                404
            );
        }

        return buildClaimResponse(claim);
    }

    @Override
    @Cacheable(value = "claims", key = "'claim-' + #claimId")
    public ClaimResponse getClaimById(long claimId) {
        log.info("Getting claim by ID: {}", claimId);

        // Validate claim ID using utility
        ClaimValidationUtil.validateClaimId(claimId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimServiceCustomException(
                    "Claim not found with ID: " + claimId,
                    "CLAIM_NOT_FOUND",
                    404
                ));

        return buildClaimResponse(claim);
    }

    private ClaimResponse buildClaimResponse(Claim claim) {
        return ClaimResponse.builder()
                .claimId(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicyId())
                .customerId(claim.getCustomerId())
                .claimAmount(claim.getClaimAmount())
                .claimType(claim.getClaimType())
                .description(claim.getDescription())
                .claimDate(claim.getClaimDate())
                .status(claim.getClaimStatus())
                .approvedAmount(claim.getApprovedAmount())
                .rejectionReason(claim.getRejectionReason())
                .build();
    }

    private String generateClaimNumber() {
        return "CLM-" + System.currentTimeMillis();
    }
} 