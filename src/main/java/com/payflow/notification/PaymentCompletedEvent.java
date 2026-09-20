package com.payflow.notification;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class PaymentCompletedEvent implements Serializable {

    private String eventId;
    private String transactionReference;
    private Long senderUserId;
    private String senderUsername;
    private Long receiverUserId;
    private String receiverUsername;
    private BigDecimal amount;
    private Instant timestamp;

    public PaymentCompletedEvent() {}

    public PaymentCompletedEvent(String eventId, String transactionReference, Long senderUserId, String senderUsername,
                                  Long receiverUserId, String receiverUsername, BigDecimal amount, Instant timestamp) {
        this.eventId = eventId;
        this.transactionReference = transactionReference;
        this.senderUserId = senderUserId;
        this.senderUsername = senderUsername;
        this.receiverUserId = receiverUserId;
        this.receiverUsername = receiverUsername;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public Long getReceiverUserId() { return receiverUserId; }
    public void setReceiverUserId(Long receiverUserId) { this.receiverUserId = receiverUserId; }
    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}