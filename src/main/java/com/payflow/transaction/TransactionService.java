package com.payflow.transaction;

import com.payflow.transaction.dto.TransactionResponseDto;
import com.payflow.wallet.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(TransactionType type, TransactionStatus status, BigDecimal amount,
                                          Wallet senderWallet, Wallet receiverWallet, String description) {
        String reference = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
        Transaction transaction = new Transaction(reference, type, status, amount, senderWallet, receiverWallet, description);
        return transactionRepository.save(transaction);
    }

    public Page<TransactionResponseDto> getUserTransactions(Long userId, TransactionType type, TransactionStatus status,
                                                              Instant from, Instant to, Pageable pageable) {
        Page<Transaction> page = transactionRepository.findUserTransactions(userId, type, status, from, to, pageable);
        return page.map(this::toDto);
    }

    private TransactionResponseDto toDto(Transaction t) {
        String senderUsername = t.getSenderWallet() != null ? t.getSenderWallet().getUser().getUsername() : null;
        String receiverUsername = t.getReceiverWallet() != null ? t.getReceiverWallet().getUser().getUsername() : null;
        return new TransactionResponseDto(
                t.getId(), t.getReference(), t.getType(), t.getStatus(), t.getAmount(),
                senderUsername, receiverUsername, t.getDescription(), t.getCreatedAt());
    }
}