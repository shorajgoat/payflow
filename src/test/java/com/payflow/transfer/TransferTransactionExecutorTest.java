package com.payflow.transfer;

import com.payflow.common.exception.InsufficientBalanceException;
import com.payflow.common.exception.InvalidRequestException;
import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.transaction.Transaction;
import com.payflow.transaction.TransactionService;
import com.payflow.transfer.dto.TransferRequest;
import com.payflow.user.Role;
import com.payflow.user.User;
import com.payflow.user.UserRepository;
import com.payflow.wallet.Wallet;
import com.payflow.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferTransactionExecutorTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionService transactionService;

    @InjectMocks private TransferTransactionExecutor executor;

    private User sender, recipient;
    private Wallet senderWallet, recipientWallet;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        sender = new User("shoraj", "shoraj@example.com", "hashed", null, Role.USER);
        sender.setId(1L);
        recipient = new User("john", "john@example.com", "hashed", null, Role.USER);
        recipient.setId(2L);

        senderWallet = new Wallet(sender, new BigDecimal("1000.00"));
        recipientWallet = new Wallet(recipient, new BigDecimal("500.00"));

        request = new TransferRequest();
        request.setRecipient("john");
        request.setAmount(new BigDecimal("200.00"));
    }

    @Test
    void transfer_success_movesBalanceCorrectly() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(recipient));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(recipientWallet));
        when(transactionService.createTransaction(any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(Transaction.class));

        executor.execute(1L, request);

        assertEquals(new BigDecimal("800.00"), senderWallet.getBalance());
        assertEquals(new BigDecimal("700.00"), recipientWallet.getBalance());
    }

    @Test
    void transfer_recipientNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> executor.execute(1L, request));
    }

    @Test
    void transfer_insufficientBalance_throwsException() {
        request.setAmount(new BigDecimal("5000.00"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(recipient));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(recipientWallet));

        assertThrows(InsufficientBalanceException.class, () -> executor.execute(1L, request));
    }

    @Test
    void transfer_toSelf_throwsException() {
        request.setRecipient("shoraj");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("shoraj")).thenReturn(Optional.of(sender));

        assertThrows(InvalidRequestException.class, () -> executor.execute(1L, request));
    }
}