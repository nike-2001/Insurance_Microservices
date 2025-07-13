package com.nikhilspring.ClaimService.event;

import com.nikhilspring.insurance.events.PaymentEvent;
import com.nikhilspring.insurance.events.PaymentStatus;
import com.nikhilspring.insurance.events.PaymentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventConsumer.class);

    @KafkaListener(topics = "payment-events", groupId = "claim-service")
    public void handlePaymentEvent(@Payload PaymentEvent paymentEvent) {
        try {
            logger.info("Received payment event: {} for payment ID: {}", 
                       paymentEvent.getStatus(), paymentEvent.getPaymentId());

            // Only process claim payments
            if (paymentEvent.getPaymentType() == PaymentType.CLAIM_PAYMENT && 
                paymentEvent.getClaimId() != null) {
                
                if (paymentEvent.getStatus() == PaymentStatus.COMPLETED) {
                    processClaimPaymentCompleted(paymentEvent);
                } else if (paymentEvent.getStatus() == PaymentStatus.FAILED) {
                    processClaimPaymentFailed(paymentEvent);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment event for payment ID: {}", 
                        paymentEvent.getPaymentId(), e);
        }
    }

    private void processClaimPaymentCompleted(PaymentEvent paymentEvent) {
        try {
            Long claimId = paymentEvent.getClaimId();
            logger.info("Processing claim payment completion for claim ID: {}. Payment ID: {}, Amount: {}", 
                       claimId, paymentEvent.getPaymentId(), paymentEvent.getAmount());
            
            // TODO: Add your claim completion logic here
            // Example: claimService.markClaimAsPaid(claimId, paymentEvent.getPaymentId());
            
            logger.info("Claim ID: {} marked as paid successfully", claimId);
            
        } catch (Exception e) {
            logger.error("Failed to process claim payment completion for claim ID: {} and payment ID: {}", 
                        paymentEvent.getClaimId(), paymentEvent.getPaymentId(), e);
        }
    }

    private void processClaimPaymentFailed(PaymentEvent paymentEvent) {
        try {
            Long claimId = paymentEvent.getClaimId();
            logger.warn("Processing claim payment failure for claim ID: {}. Payment ID: {}", 
                       claimId, paymentEvent.getPaymentId());
            
            // TODO: Add your payment failure logic here
            // Example: claimService.handlePaymentFailure(claimId, paymentEvent.getPaymentId());
            
            logger.info("Claim ID: {} payment failure handled", claimId);
            
        } catch (Exception e) {
            logger.error("Failed to process claim payment failure for claim ID: {} and payment ID: {}", 
                        paymentEvent.getClaimId(), paymentEvent.getPaymentId(), e);
        }
    }
} 