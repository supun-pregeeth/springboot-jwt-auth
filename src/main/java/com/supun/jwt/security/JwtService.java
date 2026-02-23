package com.supun.jwt.security;

import com.supun.jwt.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

@Service
public class JwtService {

    // ✅ Must be at least 32 chars for HS256
    private static final String SECRET =
            "CHANGE_THIS_TO_A_32+_CHAR_SECRET_KEY_123456";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    // Create token
    public String generateToken(String subject) {
        long now = System.currentTimeMillis();
        long exp = now + (1000L * 60 * 60); // 1 hour

        return Jwts.builder()
                .setSubject(subject)                 // ✅ 0.11.5 method
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(exp))
                .signWith(key, SignatureAlgorithm.HS256) // ✅ 0.11.5 style
                .compact();
    }

    // Extract username/email (subject)
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Validate token
    public boolean isTokenValid(String token, String subject) {
        String tokenSubject = extractSubject(token);
        return tokenSubject.equals(subject) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        // ✅ 0.11.5 parsing
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValid(String token) {
        return true;
    }

    public String extractEmail(String token) {
        return "Supun";
    }

    public String generateAccessToken(String email, Set<Role> roles) {
        return "s";
    }
}