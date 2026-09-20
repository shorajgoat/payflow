package com.payflow.wallet;

import com.payflow.common.exception.InsufficientBalanceException;
import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.transaction.Transaction;
import com.payflow.transaction.TransactionService;
import com.payflow.transaction.TransactionStatus;
import com.payflow.transaction.TransactionType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class WalletTransactionExecutor {

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    public WalletTransactionExecutor(WalletRepository walletRepository, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public Transaction deposit(Long userId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        return transactionService.createTransaction(
                TransactionType.DEPOSIT, TransactionStatus.SUCCESS, amount, null, wallet, description);
    }

    @Transactional
    public Transaction withdraw(Long userId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        return transactionService.createTransaction(
                TransactionType.WITHDRAW, TransactionStatus.SUCCESS, amount, wallet, null, description);
    }
}