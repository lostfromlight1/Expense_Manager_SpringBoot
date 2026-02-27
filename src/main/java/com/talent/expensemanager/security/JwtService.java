package com.talent.expensemanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final ApiKeyConfiguration apiKeyConfiguration;

    // 1000L (ms) * 60 (sec) * 60 (min) * 24 (hours) = 1 Day
    private static final long ONE_DAY = 1000L * 60 * 60 * 24;

    private static final long USER_ACCESS_EXPIRATION = ONE_DAY * 7;            // 7 Days
    private static final long ADMIN_ACCESS_EXPIRATION = ONE_DAY * 30;           // 30 Days
    private static final long REFRESH_TOKEN_EXPIRATION = ONE_DAY * 365;         // 1 Year

    private Key getSigningKey() {
        byte[] keyBytes = apiKeyConfiguration.getSecretToken().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String accountId, String name, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("name", name);
        claims.put("type", "ACCESS");

        long expirationTime = role.equalsIgnoreCase("ADMIN") ?
                ADMIN_ACCESS_EXPIRATION : USER_ACCESS_EXPIRATION;

        return buildToken(claims, accountId, expirationTime);
    }

    public String generateRefreshToken(String accountId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");
        return buildToken(claims, accountId, REFRESH_TOKEN_EXPIRATION);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            return "ACCESS".equals(type) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            return "REFRESH".equals(type) && !claims.getExpiration().before(new Date());
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

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}