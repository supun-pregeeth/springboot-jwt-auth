package com.supun.jwt.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 200)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser user;

    @Column(nullable = false)
    private Instant expiresAt;

    public RefreshToken(String token, AppUser user, Instant exp) {
    }
}
