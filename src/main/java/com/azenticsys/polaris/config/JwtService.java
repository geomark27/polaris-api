package com.azenticsys.polaris.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /**
     * @param tenantSchema schema PostgreSQL del tenant (ej: "t_torresytorres").
     *                     Embebido en el JWT para evitar DB lookups en cada request.
     */
    public String generateAccessToken(UUID userId, String username, String tenantSchema) {
        return buildToken(userId, username, expirationMs, Map.of(
                "type", "access",
                "tenantSchema", tenantSchema
        ));
    }

    public String generateRefreshToken(UUID userId, String username, String tenantSchema) {
        return buildToken(userId, username, refreshExpirationMs, Map.of(
                "type", "refresh",
                "tenantSchema", tenantSchema
        ));
    }

    /** Extrae el schema name del tenant embebido en el JWT. */
    public String extractTenantSchema(String token) {
        return extractClaim(token, claims -> claims.get("tenantSchema", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractClaim(token, claims -> claims.get("type", String.class)));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        String id = extractClaim(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(id);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public LocalDateTime extractExpirationAsLocalDateTime(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return Instant.ofEpochMilli(exp.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private String buildToken(UUID userId, String username, long expiration, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .claims(extraClaims)
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("userId", userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationAsDate(token).before(new Date());
    }

    private Date extractExpirationAsDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
