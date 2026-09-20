package com.payflow.wallet.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class WalletResponseDto implements Serializable {
    private Long walletId;
    private BigDecimal balance;

    public WalletResponseDto() {}

    public WalletResponseDto(Long walletId, BigDecimal balance) {
        this.walletId = walletId;
        this.balance = balance;
    }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}