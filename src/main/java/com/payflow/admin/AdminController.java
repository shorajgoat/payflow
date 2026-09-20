package com.payflow.admin;

import com.payflow.common.ApiResponse;
import com.payflow.user.User;
import com.payflow.user.UserRepository;
import com.payflow.user.dto.UserResponseDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<List<UserResponseDto>> listUsers() {
        List<UserResponseDto> users = userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        return ApiResponse.success(users);
    }

    private UserResponseDto toDto(User user) {
        return new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(),
                user.getFullName(), user.getRole(), user.getCreatedAt());
    }
}