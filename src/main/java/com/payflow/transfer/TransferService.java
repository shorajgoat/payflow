package com.payflow.transfer;

import com.payflow.common.exception.IdempotencyConflictException;
import com.payflow.notification.NotificationProducer;
import com.payflow.notification.PaymentCompletedEvent;
import com.payflow.transaction.Transaction;
import com.payflow.transaction.TransactionRepository;
import com.payflow.transfer.dto.TransferRequest;
import com.payflow.transfer.dto.TransferResponseDto;
import com.payflow.wallet.WalletService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferService {

    private static final int MAX_RETRIES = 3;

    private final TransactionRepository transactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final WalletService walletService;
    private final NotificationProducer notificationProducer;
    private final TransferTransactionExecutor transactionExecutor;

    public TransferService(TransactionRepository transactionRepository,
                            IdempotencyKeyRepository idempotencyKeyRepository,
                            WalletService walletService,
                            NotificationProducer notificationProducer,
                            TransferTransactionExecutor transactionExecutor) {
        this.transactionRepository = transactionRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.walletService = walletService;
        this.notificationProducer = notificationProducer;
        this.transactionExecutor = transactionExecutor;
    }

    public TransferResponseDto transfer(Long senderUserId, TransferRequest request, String idempotencyKeyHeader) {
        String requestHash = hashRequest(request);

        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            Optional<IdempotencyKey> existing =
                    idempotencyKeyRepository.findByKeyAndUserId(idempotencyKeyHeader, senderUserId);

            if (existing.isPresent()) {
                IdempotencyKey record = existing.get();
                if (!record.getRequestHash().equals(requestHash)) {
                    throw new IdempotencyConflictException(
                            "This idempotency key was already used with a different request payload");
                }
                if ("SUCCESS".equals(record.getStatus()) && record.getTransactionReference() != null) {
                    return buildResponseFromReference(record.getTransactionReference(), request.getRecipient());
                }
                throw new IdempotencyConflictException("A request with this idempotency key is already being processed");
            }

            try {
                idempotencyKeyRepository.save(new IdempotencyKey(idempotencyKeyHeader, senderUserId, requestHash, "PENDING"));
            } catch (DataIntegrityViolationException ex) {
                throw new IdempotencyConflictException("A request with this idempotency key is already being processed");
            }
        }

        Transaction transaction = executeWithRetry(senderUserId, request);

        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            idempotencyKeyRepository.findByKeyAndUserId(idempotencyKeyHeader, senderUserId).ifPresent(record -> {
                record.setStatus("SUCCESS");
                record.setTransactionReference(transaction.getReference());
                idempotencyKeyRepository.save(record);
            });
        }

        walletService.evictCache(senderUserId);
        walletService.evictCache(transaction.getReceiverWallet().getUser().getId());

        publishPaymentCompletedEvent(transaction);

        return new TransferResponseDto(transaction.getReference(), transaction.getStatus().name(),
                transaction.getAmount(), request.getRecipient());
    }

    private Transaction executeWithRetry(Long senderUserId, TransferRequest request) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return transactionExecutor.execute(senderUserId, request);
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (attempt == MAX_RETRIES) throw ex;
            }
        }
        throw new IllegalStateException("Transfer failed after retries");
    }

    private void publishPaymentCompletedEvent(Transaction transaction) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                transaction.getReference(),
                transaction.getSenderWallet().getUser().getId(),
                transaction.getSenderWallet().getUser().getUsername(),
                transaction.getReceiverWallet().getUser().getId(),
                transaction.getReceiverWallet().getUser().getUsername(),
                transaction.getAmount(),
                Instant.now()
        );
        notificationProducer.publishPaymentCompleted(event);
    }

    private TransferResponseDto buildResponseFromReference(String reference, String recipient) {
        Transaction transaction = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new IdempotencyConflictException("Original transaction record could not be found"));

        return new TransferResponseDto(transaction.getReference(), transaction.getStatus().name(),
                transaction.getAmount(), recipient);
    }

    private String hashRequest(TransferRequest request) {
        try {
            String raw = request.getRecipient() + "|" + request.getAmount() + "|" +
                    (request.getDescription() != null ? request.getDescription() : "");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing algorithm not available", e);
        }
    }
}