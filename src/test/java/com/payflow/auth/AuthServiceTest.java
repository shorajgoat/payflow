package com.payflow.auth;

import com.payflow.auth.dto.LoginRequest;
import com.payflow.auth.dto.RegisterRequest;
import com.payflow.common.exception.DuplicateResourceException;
import com.payflow.user.Role;
import com.payflow.user.User;
import com.payflow.user.UserRepository;
import com.payflow.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private WalletService walletService;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("shoraj");
        registerRequest.setEmail("shoraj@example.com");
        registerRequest.setPassword("password123");
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("shoraj")).thenReturn(false);
        when(userRepository.existsByEmail("shoraj@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        User savedUser = new User("shoraj", "shoraj@example.com", "hashed", null, Role.USER);
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(1L, "shoraj", "USER")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L, "shoraj")).thenReturn("refresh-token");

        var response = authService.register(registerRequest);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("shoraj", response.getUser().getUsername());
        verify(walletService).createWalletForUser(savedUser);
    }

    @Test
    void register_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername("shoraj")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByUsername("shoraj")).thenReturn(false);
        when(userRepository.existsByEmail("shoraj@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_invalidPassword_throwsBadCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("shoraj");
        loginRequest.setPassword("wrongpassword");

        doThrow(new org.springframework.security.authentication.BadCredentialsException("bad"))
                .when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("shoraj");
        loginRequest.setPassword("password123");

        User user = new User("shoraj", "shoraj@example.com", "hashed", null, Role.USER);
        user.setId(1L);
        when(userRepository.findByUsername("shoraj")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(1L, "shoraj", "USER")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L, "shoraj")).thenReturn("refresh-token");

        var response = authService.login(loginRequest);

        assertEquals("access-token", response.getAccessToken());
    }
}