package com.nikhilspring.PaymentService.event;

import com.nikhilspring.insurance.events.PaymentEvent;
import com.nikhilspring.insurance.events.PaymentStatus;
import com.nikhilspring.insurance.events.PaymentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String PAYMENT_TOPIC = "payment-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentEvent(Long paymentId, Long policyId, Long claimId, 
                                   Double amount, PaymentStatus status, PaymentType type) {
        try {
            PaymentEvent paymentEvent = PaymentEvent.newBuilder()
                    .setPaymentId(paymentId)
                    .setPolicyId(policyId)
                    .setClaimId(claimId)
                    .setAmount(amount)
                    .setStatus(status)
                    .setPaymentType(type)
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            logger.info("Publishing payment event: {} for payment ID: {}", status, paymentId);
            kafkaTemplate.send(PAYMENT_TOPIC, String.valueOf(paymentId), paymentEvent);
            logger.info("Payment event published successfully");
            
        } catch (Exception e) {
            logger.error("Error publishing payment event for payment ID: {}", paymentId, e);
            throw new RuntimeException("Failed to publish payment event", e);
        }
    }

    // Convenience methods for common scenarios
    public void publishPolicyPaymentCompleted(Long paymentId, Long policyId, Double amount) {
        publishPaymentEvent(paymentId, policyId, null, amount, 
                          PaymentStatus.COMPLETED, PaymentType.PREMIUM);
    }

    public void publishClaimPaymentCompleted(Long paymentId, Long claimId, Double amount) {
        publishPaymentEvent(paymentId, null, claimId, amount, 
                          PaymentStatus.COMPLETED, PaymentType.CLAIM_PAYMENT);
    }
} 