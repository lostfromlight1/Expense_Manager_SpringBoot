package com.talent.expensemanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final ApiKeyConfiguration apiKeyConfiguration;

    @Value("${jwt.expiration.user-access}")
    private long userAccessExpiration;

    @Value("${jwt.expiration.admin-access}")
    private long adminAccessExpiration;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpiration;


    private SecretKey getSigningKey() {
        byte[] keyBytes = apiKeyConfiguration.getSecretToken().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String generateToken(String accountId, String name, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("name", name);
        claims.put("type", "ACCESS");

        long expirationTime = role.equalsIgnoreCase("ADMIN") ?
                adminAccessExpiration : userAccessExpiration;

        return buildToken(claims, accountId, expirationTime);
    }

    public String generateRefreshToken(String accountId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");
        return buildToken(claims, accountId, refreshTokenExpiration);
    }
    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            return "ACCESS".equals(type) && isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            return "REFRESH".equals(type) && isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractAccountId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        return !claims.getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}