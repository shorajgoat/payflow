package com.payflow.notification;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private static final String TOPIC = "payment-completed-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.getTransactionReference(), event);
        } catch (Exception ex) {
            System.err.println("Failed to publish PaymentCompletedEvent: " + ex.getMessage());
        }
    }
}