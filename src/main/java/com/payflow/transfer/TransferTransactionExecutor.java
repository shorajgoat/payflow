package com.payflow.transfer;

import com.payflow.common.exception.InsufficientBalanceException;
import com.payflow.common.exception.InvalidRequestException;
import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.transaction.Transaction;
import com.payflow.transaction.TransactionService;
import com.payflow.transaction.TransactionStatus;
import com.payflow.transaction.TransactionType;
import com.payflow.transfer.dto.TransferRequest;
import com.payflow.user.User;
import com.payflow.user.UserRepository;
import com.payflow.wallet.Wallet;
import com.payflow.wallet.WalletRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransferTransactionExecutor {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    public TransferTransactionExecutor(UserRepository userRepository, WalletRepository walletRepository,
                                        TransactionService transactionService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public Transaction execute(Long senderUserId, TransferRequest request) {
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User recipient = userRepository.findByUsername(request.getRecipient())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        if (sender.getId().equals(recipient.getId())) {
            throw new InvalidRequestException("Cannot transfer money to yourself");
        }

        Wallet senderWallet = walletRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender wallet not found"));
        Wallet receiverWallet = walletRepository.findByUserId(recipient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient wallet not found"));

        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        return transactionService.createTransaction(
                TransactionType.TRANSFER, TransactionStatus.SUCCESS, request.getAmount(),
                senderWallet, receiverWallet, request.getDescription());
    }
}