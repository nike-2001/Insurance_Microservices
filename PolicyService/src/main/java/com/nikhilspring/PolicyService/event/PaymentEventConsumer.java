package com.nikhilspring.PolicyService.event;

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

    @KafkaListener(topics = "payment-events", groupId = "policy-service")
    public void handlePaymentEvent(@Payload PaymentEvent paymentEvent) {
        try {
            logger.info("Received payment event: {} for payment ID: {}", 
                       paymentEvent.getStatus(), paymentEvent.getPaymentId());

            // Only process premium payments that are completed
            if (paymentEvent.getPaymentType() == PaymentType.PREMIUM && 
                paymentEvent.getStatus() == PaymentStatus.COMPLETED &&
                paymentEvent.getPolicyId() != null) {
                
                activatePolicy(paymentEvent);
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment event for payment ID: {}", 
                        paymentEvent.getPaymentId(), e);
        }
    }

    private void activatePolicy(PaymentEvent paymentEvent) {
        try {
            Long policyId = paymentEvent.getPolicyId();
            logger.info("Activating policy ID: {} due to payment completion. Payment ID: {}, Amount: {}", 
                       policyId, paymentEvent.getPaymentId(), paymentEvent.getAmount());
            
            // TODO: Add your policy activation logic here
            // Example: policyService.activatePolicy(policyId);
            
            logger.info("Policy ID: {} activated successfully", policyId);
            
        } catch (Exception e) {
            logger.error("Failed to activate policy ID: {} for payment ID: {}", 
                        paymentEvent.getPolicyId(), paymentEvent.getPaymentId(), e);
        }
    }
} 