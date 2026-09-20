package com.payflow.transfer.dto;

import java.math.BigDecimal;

public class TransferResponseDto {

    private String reference;
    private String status;
    private BigDecimal amount;
    private String recipientUsername;

    public TransferResponseDto(String reference, String status, BigDecimal amount, String recipientUsername) {
        this.reference = reference;
        this.status = status;
        this.amount = amount;
        this.recipientUsername = recipientUsername;
    }

    public String getReference() { return reference; }
    public String getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getRecipientUsername() { return recipientUsername; }
}