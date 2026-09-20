package com.payflow.transfer;

import com.payflow.common.ApiResponse;
import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.transfer.dto.TransferRequest;
import com.payflow.transfer.dto.TransferResponseDto;
import com.payflow.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;
    private final UserRepository userRepository;

    public TransferController(TransferService transferService, UserRepository userRepository) {
        this.transferService = transferService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ApiResponse<TransferResponseDto> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        Long senderUserId = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();

        TransferResponseDto result = transferService.transfer(senderUserId, request, idempotencyKey);
        return ApiResponse.success("Transfer completed", result);
    }
}