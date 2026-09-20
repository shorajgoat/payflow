package com.payflow.user;

import com.payflow.common.ApiResponse;
import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.user.dto.UserResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserResponseDto dto = new UserResponseDto(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getFullName(), user.getRole(), user.getCreatedAt());

        return ApiResponse.success(dto);
    }
}