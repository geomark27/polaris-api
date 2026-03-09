package com.azenticsys.polaris.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(schema = "landlord", name = "revoked_tokens", indexes = {
        @Index(name = "idx_revoked_tokens_jti", columnList = "jti")
})
public class RevokedToken {

    @Id
    @Column(name = "jti", nullable = false, updatable = false)
    private String jti;

    @Column(name = "revoked_at", nullable = false, updatable = false)
    private LocalDateTime revokedAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    protected RevokedToken() {}

    public RevokedToken(String jti, LocalDateTime expiresAt) {
        this.jti = jti;
        this.revokedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public String getJti() { return jti; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
