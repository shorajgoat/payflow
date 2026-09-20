package com.payflow.auth;

import com.payflow.auth.dto.*;
import com.payflow.common.exception.DuplicateResourceException;
import com.payflow.common.exception.InvalidRequestException;
import com.payflow.user.Role;
import com.payflow.user.User;
import com.payflow.user.UserRepository;
import com.payflow.wallet.WalletService;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final WalletService walletService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtService jwtService, AuthenticationManager authenticationManager,
                        WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.walletService = walletService;
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());
        AuthResponse.UserSummary summary = new AuthResponse.UserSummary(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getRole().name());
        return new AuthResponse(accessToken, refreshToken, summary);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                Role.USER
        );
        user = userRepository.save(user);
        walletService.createWalletForUser(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.getRefreshToken();
        try {
            String type = jwtService.extractTokenType(token);
            if (!"refresh".equals(type)) {
                throw new InvalidRequestException("Provided token is not a refresh token");
            }
            String username = jwtService.extractUsername(token);
            if (jwtService.isTokenExpired(token)) {
                throw new InvalidRequestException("Refresh token has expired");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidRequestException("User not found"));

            return buildAuthResponse(user);
        } catch (JwtException ex) {
            throw new InvalidRequestException("Invalid or malformed refresh token");
        }
    }
}