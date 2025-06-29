package com.nikhilspring.ClaimService.validation;

import com.nikhilspring.ClaimService.exception.ClaimServiceCustomException;
import com.nikhilspring.ClaimService.model.ClaimRequest;

public class ClaimValidationUtil {

    public static void validateClaimRequest(ClaimRequest claimRequest) {
        if (claimRequest == null) {
            throw new ClaimServiceCustomException(
                "Claim request cannot be null",
                "INVALID_REQUEST",
                400
            );
        }

        if (claimRequest.getPolicyId() <= 0) {
            throw new ClaimServiceCustomException(
                "Invalid policy ID: " + claimRequest.getPolicyId(),
                "INVALID_POLICY_ID",
                400
            );
        }

        if (claimRequest.getClaimAmount() <= 0) {
            throw new ClaimServiceCustomException(
                "Claim amount must be greater than 0",
                "INVALID_CLAIM_AMOUNT",
                400
            );
        }

        if (claimRequest.getCustomerId() == null || claimRequest.getCustomerId().trim().isEmpty()) {
            throw new ClaimServiceCustomException(
                "Customer ID cannot be null or empty",
                "INVALID_CUSTOMER_ID",
                400
            );
        }

        // Validate customer ID format
        if (!claimRequest.getCustomerId().matches("^[A-Z0-9]+$")) {
            throw new ClaimServiceCustomException(
                "Invalid customer ID format. Must contain only uppercase letters and numbers: " + claimRequest.getCustomerId(),
                "INVALID_CUSTOMER_ID_FORMAT",
                400
            );
        }

        if (claimRequest.getClaimType() == null || claimRequest.getClaimType().trim().isEmpty()) {
            throw new ClaimServiceCustomException(
                "Claim type cannot be null or empty",
                "INVALID_CLAIM_TYPE",
                400
            );
        }

        if (claimRequest.getDescription() == null || claimRequest.getDescription().trim().isEmpty()) {
            throw new ClaimServiceCustomException(
                "Claim description cannot be null or empty",
                "INVALID_DESCRIPTION",
                400
            );
        }
    }

    public static void validateClaimId(long claimId) {
        if (claimId <= 0) {
            throw new ClaimServiceCustomException(
                "Invalid claim ID: " + claimId,
                "INVALID_CLAIM_ID",
                400
            );
        }
    }

    public static void validatePolicyId(long policyId) {
        if (policyId <= 0) {
            throw new ClaimServiceCustomException(
                "Invalid policy ID: " + policyId,
                "INVALID_POLICY_ID",
                400
            );
        }
    }
} 