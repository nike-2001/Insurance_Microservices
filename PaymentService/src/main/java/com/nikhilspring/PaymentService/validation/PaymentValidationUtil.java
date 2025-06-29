package com.nikhilspring.PaymentService.validation;

import com.nikhilspring.PaymentService.exception.PaymentServiceCustomException;
import com.nikhilspring.PaymentService.model.PaymentRequest;

public class PaymentValidationUtil {

    public static void validatePaymentRequest(PaymentRequest paymentRequest) {
        if (paymentRequest == null) {
            throw new PaymentServiceCustomException(
                "Payment request cannot be null",
                "INVALID_REQUEST",
                400
            );
        }

        if (paymentRequest.getPolicyId() <= 0) {
            throw new PaymentServiceCustomException(
                "Invalid policy ID: " + paymentRequest.getPolicyId(),
                "INVALID_POLICY_ID",
                400
            );
        }

        if (paymentRequest.getAmount() <= 0) {
            throw new PaymentServiceCustomException(
                "Payment amount must be greater than 0",
                "INVALID_AMOUNT",
                400
            );
        }

        if (paymentRequest.getPaymentMode() == null) {
            throw new PaymentServiceCustomException(
                "Payment mode cannot be null",
                "INVALID_PAYMENT_MODE",
                400
            );
        }

        if (paymentRequest.getPaymentType() == null || paymentRequest.getPaymentType().trim().isEmpty()) {
            throw new PaymentServiceCustomException(
                "Payment type cannot be null or empty",
                "INVALID_PAYMENT_TYPE",
                400
            );
        }

        if (paymentRequest.getCustomerId() == null || paymentRequest.getCustomerId().trim().isEmpty()) {
            throw new PaymentServiceCustomException(
                "Customer ID cannot be null or empty",
                "INVALID_CUSTOMER_ID",
                400
            );
        }

        if (paymentRequest.getReferenceNumber() == null || paymentRequest.getReferenceNumber().trim().isEmpty()) {
            throw new PaymentServiceCustomException(
                "Reference number cannot be null or empty",
                "INVALID_REFERENCE_NUMBER",
                400
            );
        }

        if (paymentRequest.getPolicyNumber() == null || paymentRequest.getPolicyNumber().trim().isEmpty()) {
            throw new PaymentServiceCustomException(
                "Policy number cannot be null or empty",
                "INVALID_POLICY_NUMBER",
                400
            );
        }

        // Validate payment type
        validatePaymentType(paymentRequest.getPaymentType());
    }

    public static void validatePaymentType(String paymentType) {
        if (paymentType == null) {
            throw new PaymentServiceCustomException(
                "Payment type cannot be null",
                "INVALID_PAYMENT_TYPE",
                400
            );
        }

        String upperCaseType = paymentType.toUpperCase();
        if (!upperCaseType.equals("PREMIUM") && 
            !upperCaseType.equals("RENEWAL") && 
            !upperCaseType.equals("CLAIM_PAYMENT") && 
            !upperCaseType.equals("ADMINISTRATIVE_FEE")) {
            throw new PaymentServiceCustomException(
                "Invalid payment type: " + paymentType + ". Valid types are: PREMIUM, RENEWAL, CLAIM_PAYMENT, ADMINISTRATIVE_FEE",
                "INVALID_PAYMENT_TYPE",
                400
            );
        }
    }

    public static void validatePolicyId(String policyId) {
        if (policyId == null || policyId.trim().isEmpty()) {
            throw new PaymentServiceCustomException(
                "Policy ID cannot be null or empty",
                "INVALID_POLICY_ID",
                400
            );
        }

        try {
            Long.valueOf(policyId);
        } catch (NumberFormatException e) {
            throw new PaymentServiceCustomException(
                "Invalid policy ID format: " + policyId,
                "INVALID_POLICY_ID_FORMAT",
                400
            );
        }
    }

    public static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentServiceCustomException(
                "Customer ID cannot be null or empty",
                "INVALID_CUSTOMER_ID",
                400
            );
        }
    }
} 