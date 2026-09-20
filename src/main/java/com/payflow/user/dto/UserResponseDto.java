package com.payflow.user.dto;

import com.payflow.user.Role;
import java.time.Instant;

public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private Instant createdAt;

    public UserResponseDto(Long id, String username, String email, String fullName, Role role, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}