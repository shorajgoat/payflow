package com.payflow.transaction.dto;

import com.payflow.transaction.TransactionStatus;
import com.payflow.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponseDto {

    private Long id;
    private String reference;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private String senderUsername;
    private String receiverUsername;
    private String description;
    private Instant createdAt;

    public TransactionResponseDto(Long id, String reference, TransactionType type, TransactionStatus status,
                                   BigDecimal amount, String senderUsername, String receiverUsername,
                                   String description, Instant createdAt) {
        this.id = id;
        this.reference = reference;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getSenderUsername() { return senderUsername; }
    public String getReceiverUsername() { return receiverUsername; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}