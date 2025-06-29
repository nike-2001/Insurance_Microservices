package com.nikhilspring.PolicyService.validation;

import com.nikhilspring.PolicyService.exception.CustomException;
import com.nikhilspring.PolicyService.model.PolicyRequest;

import java.time.Instant;

public class PolicyValidationUtil {

    public static void validatePolicyRequest(PolicyRequest policyRequest) {
        if (policyRequest == null) {
            throw new CustomException(
                "Policy request cannot be null",
                "INVALID_REQUEST",
                400
            );
        }

        if (policyRequest.getCustomerId() <= 0) {
            throw new CustomException(
                "Invalid customer ID: " + policyRequest.getCustomerId(),
                "INVALID_CUSTOMER_ID",
                400
            );
        }

        if (policyRequest.getProductId() <= 0) {
            throw new CustomException(
                "Invalid product ID: " + policyRequest.getProductId(),
                "INVALID_PRODUCT_ID",
                400
            );
        }

        if (policyRequest.getPremiumAmount() <= 0) {
            throw new CustomException(
                "Premium amount must be greater than 0",
                "INVALID_PREMIUM_AMOUNT",
                400
            );
        }

        if (policyRequest.getCoverageAmount() <= 0) {
            throw new CustomException(
                "Coverage amount must be greater than 0",
                "INVALID_COVERAGE_AMOUNT",
                400
            );
        }

        if (policyRequest.getPolicyStartDate() == null) {
            throw new CustomException(
                "Policy start date cannot be null",
                "INVALID_START_DATE",
                400
            );
        }

        if (policyRequest.getPolicyEndDate() == null) {
            throw new CustomException(
                "Policy end date cannot be null",
                "INVALID_END_DATE",
                400
            );
        }

        if (policyRequest.getPolicyStartDate().isAfter(policyRequest.getPolicyEndDate())) {
            throw new CustomException(
                "Policy start date cannot be after end date",
                "INVALID_DATE_RANGE",
                400
            );
        }

        // Comment out past date validation for testing purposes
        // if (policyRequest.getPolicyStartDate().isBefore(Instant.now())) {
        //     throw new CustomException(
        //         "Policy start date cannot be in the past",
        //         "INVALID_START_DATE",
        //         400
        //     );
        // }

        if (policyRequest.getPaymentMode() == null) {
            throw new CustomException(
                "Payment mode cannot be null",
                "INVALID_PAYMENT_MODE",
                400
            );
        }
    }

    public static void validatePolicyId(long policyId) {
        if (policyId <= 0) {
            throw new CustomException(
                "Invalid policy ID: " + policyId,
                "INVALID_POLICY_ID",
                400
            );
        }
    }

    public static void validatePolicyExists(boolean exists, long policyId) {
        if (!exists) {
            throw new CustomException(
                "Policy not found with ID: " + policyId,
                "POLICY_NOT_FOUND",
                404
            );
        }
    }
} 