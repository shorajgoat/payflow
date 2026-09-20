package com.payflow.wallet;

import com.payflow.common.ApiResponse;
import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.transaction.Transaction;
import com.payflow.user.UserRepository;
import com.payflow.wallet.dto.DepositRequest;
import com.payflow.wallet.dto.WalletResponseDto;
import com.payflow.wallet.dto.WithdrawRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    public WalletController(WalletService walletService, UserRepository userRepository) {
        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    private Long currentUserId(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @GetMapping
    public ApiResponse<WalletResponseDto> getBalance(Authentication authentication) {
        return ApiResponse.success(walletService.getBalance(currentUserId(authentication)));
    }

    @PostMapping("/deposit")
    public ApiResponse<Map<String, Object>> deposit(Authentication authentication, @Valid @RequestBody DepositRequest request) {
        Transaction txn = walletService.deposit(currentUserId(authentication), request.getAmount(), request.getDescription());
        return ApiResponse.success("Deposit successful", Map.of(
                "reference", txn.getReference(),
                "amount", txn.getAmount(),
                "status", txn.getStatus()
        ));
    }

    @PostMapping("/withdraw")
    public ApiResponse<Map<String, Object>> withdraw(Authentication authentication, @Valid @RequestBody WithdrawRequest request) {
        Transaction txn = walletService.withdraw(currentUserId(authentication), request.getAmount(), request.getDescription());
        return ApiResponse.success("Withdrawal successful", Map.of(
                "reference", txn.getReference(),
                "amount", txn.getAmount(),
                "status", txn.getStatus()
        ));
    }
}