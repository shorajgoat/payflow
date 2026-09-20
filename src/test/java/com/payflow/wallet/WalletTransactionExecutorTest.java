package com.payflow.wallet;

import com.payflow.common.exception.InsufficientBalanceException;
import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.transaction.Transaction;
import com.payflow.transaction.TransactionService;
import com.payflow.transaction.TransactionStatus;
import com.payflow.transaction.TransactionType;
import com.payflow.user.Role;
import com.payflow.user.User;
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
class WalletTransactionExecutorTest {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionService transactionService;

    @InjectMocks private WalletTransactionExecutor executor;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        User user = new User("shoraj", "shoraj@example.com", "hashed", null, Role.USER);
        user.setId(1L);
        wallet = new Wallet(user, new BigDecimal("1000.00"));
    }

    @Test
    void deposit_success_increasesBalance() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionService.createTransaction(eq(TransactionType.DEPOSIT), eq(TransactionStatus.SUCCESS),
                any(), any(), any(), any())).thenReturn(mock(Transaction.class));

        executor.deposit(1L, new BigDecimal("500.00"), "test deposit");

        assertEquals(new BigDecimal("1500.00"), wallet.getBalance());
    }

    @Test
    void deposit_walletNotFound_throwsException() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> executor.deposit(1L, new BigDecimal("500.00"), "test"));
    }

    @Test
    void withdraw_success_decreasesBalance() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionService.createTransaction(eq(TransactionType.WITHDRAW), eq(TransactionStatus.SUCCESS),
                any(), any(), any(), any())).thenReturn(mock(Transaction.class));

        executor.withdraw(1L, new BigDecimal("300.00"), "test withdraw");

        assertEquals(new BigDecimal("700.00"), wallet.getBalance());
    }

    @Test
    void withdraw_insufficientBalance_throwsException() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class,
                () -> executor.withdraw(1L, new BigDecimal("5000.00"), "test"));

        assertEquals(new BigDecimal("1000.00"), wallet.getBalance());
    }
}