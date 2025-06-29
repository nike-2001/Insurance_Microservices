package com.nikhilspring.PaymentService.service;

import com.nikhilspring.PaymentService.entity.TransactionDetails;
import com.nikhilspring.PaymentService.exception.PaymentServiceCustomException;
import com.nikhilspring.PaymentService.model.PaymentMode;
import com.nikhilspring.PaymentService.model.PaymentRequest;
import com.nikhilspring.PaymentService.model.PaymentResponse;
import com.nikhilspring.PaymentService.repository.TransactionDetailsRepository;
import com.nikhilspring.PaymentService.validation.PaymentValidationUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private TransactionDetailsRepository transactionDetailsRepository;

    @Override
    public long processPayment(PaymentRequest paymentRequest) {
        log.info("Processing Payment: {}", paymentRequest);

        // Validate payment request using utility
        PaymentValidationUtil.validatePaymentRequest(paymentRequest);

        // Validate policy exists and is active
        validatePolicyExists(paymentRequest.getPolicyId(), paymentRequest.getPolicyNumber());

        // Check if payment already exists for this policy
        Optional<TransactionDetails> existingPayment = transactionDetailsRepository.findByPolicyId(paymentRequest.getPolicyId());
        if (existingPayment.isPresent()) {
            throw new PaymentServiceCustomException(
                "Payment already exists for policy ID: " + paymentRequest.getPolicyId(),
                "PAYMENT_ALREADY_EXISTS",
                409
            );
        }

        // Validate payment amount against policy premium
        validatePaymentAmount(paymentRequest.getAmount(), paymentRequest.getPolicyId());

        TransactionDetails transactionDetails
                = TransactionDetails.builder()
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentType(paymentRequest.getPaymentType())
                .paymentStatus("SUCCESS")
                .policyId(paymentRequest.getPolicyId())
                .customerId(paymentRequest.getCustomerId())
                .policyNumber(paymentRequest.getPolicyNumber())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .transactionId(generateTransactionId())
                .amount(paymentRequest.getAmount())
                .description(paymentRequest.getDescription())
                .build();

        try {
            transactionDetailsRepository.save(transactionDetails);
            log.info("Payment Transaction Completed with Id: {}", transactionDetails.getId());
            return transactionDetails.getId();
        } catch (Exception e) {
            log.error("Error saving payment transaction: {}", e.getMessage());
            throw new PaymentServiceCustomException(
                "Failed to process payment: " + e.getMessage(),
                "PAYMENT_PROCESSING_FAILED",
                500
            );
        }
    }

    private void validatePolicyExists(long policyId, String policyNumber) {
        log.info("Validating policy exists: ID={}, Number={}", policyId, policyNumber);
        
        // For now, we'll do basic validation
        // TODO: Add actual policy service call when Feign is available
        if (policyId <= 0) {
            throw new PaymentServiceCustomException(
                "Invalid policy ID: " + policyId,
                "INVALID_POLICY_ID",
                400
            );
        }
        
        if (policyNumber == null || policyNumber.trim().isEmpty()) {
            throw new PaymentServiceCustomException(
                "Policy number cannot be null or empty",
                "INVALID_POLICY_NUMBER",
                400
            );
        }
        
        // TODO: Add call to PolicyService to validate policy exists and is active
        log.info("Policy validation passed for ID: {}", policyId);
    }

    private void validatePaymentAmount(long paymentAmount, long policyId) {
        log.info("Validating payment amount: {} for policy ID: {}", paymentAmount, policyId);
        
        // TODO: Add call to PolicyService to get policy premium amount
        // For now, we'll do basic validation
        if (paymentAmount <= 0) {
            throw new PaymentServiceCustomException(
                "Payment amount must be greater than 0",
                "INVALID_PAYMENT_AMOUNT",
                400
            );
        }
        
        // TODO: Compare payment amount with policy premium amount
        log.info("Payment amount validation passed: {}", paymentAmount);
    }

    @Override
    public PaymentResponse getPaymentDetailsByPolicyId(String policyId) {
        log.info("Getting payment details for the Policy Id: {}", policyId);

        // Validate policy ID using utility
        PaymentValidationUtil.validatePolicyId(policyId);

        try {
            Long policyIdLong = Long.valueOf(policyId);
            Optional<TransactionDetails> transactionDetailsOpt = transactionDetailsRepository.findByPolicyId(policyIdLong);

            if (transactionDetailsOpt.isEmpty()) {
                throw new PaymentServiceCustomException(
                    "Payment not found for policy ID: " + policyId,
                    "PAYMENT_NOT_FOUND",
                    404
                );
            }

            return buildPaymentResponse(transactionDetailsOpt.get());
        } catch (NumberFormatException e) {
            throw new PaymentServiceCustomException(
                "Invalid policy ID format: " + policyId,
                "INVALID_POLICY_ID_FORMAT",
                400
            );
        }
    }

    @Override
    public PaymentResponse getPaymentDetailsByCustomerId(String customerId) {
        log.info("Getting payment details for the Customer Id: {}", customerId);

        // Validate customer ID using utility
        PaymentValidationUtil.validateCustomerId(customerId);

        Optional<TransactionDetails> transactionDetailsOpt = transactionDetailsRepository.findByCustomerId(customerId);

        if (transactionDetailsOpt.isEmpty()) {
            throw new PaymentServiceCustomException(
                "Payment not found for customer ID: " + customerId,
                "PAYMENT_NOT_FOUND",
                404
            );
        }

        return buildPaymentResponse(transactionDetailsOpt.get());
    }

    private PaymentResponse buildPaymentResponse(TransactionDetails transactionDetails) {
        return PaymentResponse.builder()
                .paymentId(transactionDetails.getId())
                .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                .paymentDate(transactionDetails.getPaymentDate())
                .policyId(transactionDetails.getPolicyId())
                .status(transactionDetails.getPaymentStatus())
                .amount(transactionDetails.getAmount())
                .paymentType(transactionDetails.getPaymentType())
                .referenceNumber(transactionDetails.getReferenceNumber())
                .customerId(transactionDetails.getCustomerId())
                .policyNumber(transactionDetails.getPolicyNumber())
                .transactionId(transactionDetails.getTransactionId())
                .build();
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}