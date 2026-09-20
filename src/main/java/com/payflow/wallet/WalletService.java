package com.payflow.wallet;

import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.transaction.Transaction;
import com.payflow.user.User;
import com.payflow.wallet.dto.WalletResponseDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class WalletService {

    private static final int MAX_RETRIES = 3;
    private static final String CACHE_PREFIX = "wallet:balance:";

    private final WalletRepository walletRepository;
    private final WalletTransactionExecutor executor;
    private final RedisTemplate<String, Object> redisTemplate;

    public WalletService(WalletRepository walletRepository, WalletTransactionExecutor executor,
                          RedisTemplate<String, Object> redisTemplate) {
        this.walletRepository = walletRepository;
        this.executor = executor;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Wallet createWalletForUser(User user) {
        Wallet wallet = new Wallet(user, BigDecimal.ZERO.setScale(2));
        return walletRepository.save(wallet);
    }

    public WalletResponseDto getBalance(Long userId) {
        String cacheKey = CACHE_PREFIX + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached instanceof WalletResponseDto dto) {
            return dto;
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));

        WalletResponseDto dto = new WalletResponseDto(wallet.getId(), wallet.getBalance());
        redisTemplate.opsForValue().set(cacheKey, dto, 60, TimeUnit.SECONDS);
        return dto;
    }

    public Transaction deposit(Long userId, BigDecimal amount, String description) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Transaction txn = executor.deposit(userId, amount, description);
                evictCache(userId);
                return txn;
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (attempt == MAX_RETRIES) throw ex;
            }
        }
        throw new IllegalStateException("Deposit failed after retries");
    }

    public Transaction withdraw(Long userId, BigDecimal amount, String description) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Transaction txn = executor.withdraw(userId, amount, description);
                evictCache(userId);
                return txn;
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (attempt == MAX_RETRIES) throw ex;
            }
        }
        throw new IllegalStateException("Withdraw failed after retries");
    }

    public void evictCache(Long userId) {
        redisTemplate.delete(CACHE_PREFIX + userId);
    }
}