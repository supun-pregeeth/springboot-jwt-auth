package com.supun.jwt.service;

import com.supun.jwt.dto.AuthRequest;
import com.supun.jwt.dto.AuthResponse;
import com.supun.jwt.dto.RegisterRequest;
import com.supun.jwt.entity.AppUser;
import com.supun.jwt.entity.RefreshToken;
import com.supun.jwt.entity.Role;
import com.supun.jwt.repo.RefreshTokenRepository;
import com.supun.jwt.repo.UserRepository;
import com.supun.jwt.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final long refreshExpMs;

    public AuthService(
            UserRepository users,
            RefreshTokenRepository refreshTokens,
            PasswordEncoder encoder,
            AuthenticationManager authManager,
            JwtService jwtService,
            @Value("${app.jwt.refresh-exp-ms}") long refreshExpMs
    ) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.refreshExpMs = refreshExpMs;
    }

    public void register(RegisterRequest req) {
        if (users.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role;
        try {
            role = Role.valueOf(req.getRole().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid role. Use USER or ADMIN");
        }

        var user = new AppUser(
                req.getEmail(),
                encoder.encode(req.getPassword()),
                Set.of(role)
        );
        users.save(user);
    }

    public AuthResponse login(AuthRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        var user = users.findByEmail(req.getEmail()).orElseThrow();
        String access = jwtService.generateAccessToken(user.getEmail(), user.getRoles());
        String refresh = createRefreshToken(user);

        return new AuthResponse(access, refresh);
    }

    public AuthResponse refresh(String refreshToken) {
        var stored = refreshTokens.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokens.deleteByToken(refreshToken);
            throw new IllegalArgumentException("Refresh token expired");
        }

        var user = stored.getUser();
        String newAccess = jwtService.generateAccessToken(user.getEmail(), user.getRoles());
        return new AuthResponse(newAccess, refreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokens.deleteByToken(refreshToken);
    }

    private String createRefreshToken(AppUser user) {
        String token = randomToken(48);
        Instant exp = Instant.now().plusMillis(refreshExpMs);
        refreshTokens.save(new RefreshToken(token, user, exp));
        return token;
    }

    private static String randomToken(int bytes) {
        byte[] buf = new byte[bytes];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}