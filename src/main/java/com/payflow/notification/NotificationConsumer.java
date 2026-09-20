package com.payflow.notification;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "payment-completed-events", groupId = "payflow-notification-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        String message = "Payment received: NPR " + event.getAmount() + " from " + event.getSenderUsername();

        messagingTemplate.convertAndSendToUser(
                event.getReceiverUsername(),
                "/queue/notifications",
                Map.of(
                        "type", "PAYMENT_RECEIVED",
                        "message", message,
                        "reference", event.getTransactionReference(),
                        "amount", event.getAmount(),
                        "from", event.getSenderUsername()
                )
        );
    }
}