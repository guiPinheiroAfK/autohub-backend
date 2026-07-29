package com.autohub.domain.service;

import com.autohub.domain.model.User;
import com.autohub.domain.repository.UserRepository;
import com.autohub.infrastructure.cache.TokenBlacklistService;
import com.autohub.infrastructure.security.JwtService;
import com.autohub.shared.exception.exceptions.EmailAlreadyInUseException;
import com.autohub.shared.exception.exceptions.InvalidTokenException;
import com.autohub.web.dto.auth.AuthResponse;
import com.autohub.web.dto.auth.LoginRequest;
import com.autohub.web.dto.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService blacklistService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidTokenException("User not found"));
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken) || blacklistService.isBlacklisted(refreshToken)) {
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }
        String email = jwtService.extractSubject(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found"));
        blacklistService.blacklist(refreshToken, jwtService.getExpirationMs(refreshToken));
        return issueTokens(user);
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && jwtService.isTokenValid(accessToken)) {
            blacklistService.blacklist(accessToken, jwtService.getExpirationMs(accessToken));
        }
        if (refreshToken != null && jwtService.isTokenValid(refreshToken)) {
            blacklistService.blacklist(refreshToken, jwtService.getExpirationMs(refreshToken));
        }
    }

    private AuthResponse issueTokens(User user) {
        Map<String, Object> claims = Map.of("role", user.getRole().name(), "name", user.getName());
        String accessToken  = jwtService.generateAccessToken(user.getEmail(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return AuthResponse.of(accessToken, refreshToken);
    }
}
