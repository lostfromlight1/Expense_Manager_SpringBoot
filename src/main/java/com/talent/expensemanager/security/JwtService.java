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

    // Constants for Token Durations
    private static final long USER_ACCESS_EXPIRATION = 1000L * 60 * 60 * 24 * 7 ;      // 1 Year
    private static final long ADMIN_ACCESS_EXPIRATION = 1000L * 60 * 60 * 24 * 365 * 2; // 2 Years
    private static final long REFRESH_TOKEN_EXPIRATION =1000L * 60 * 60 * 24 * 365 ;      // 7 Days

    private Key getSigningKey() {
        byte[] keyBytes = apiKeyConfiguration.getSecretToken().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates an Access Token with logic to give Admins a longer lifespan
     */
    public String generateToken(String accountId, String name, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("name", name);

        // Logic: If Admin, give 2 years, else 1 year
        long expirationTime = role.equalsIgnoreCase("ADMIN") ?
                ADMIN_ACCESS_EXPIRATION : USER_ACCESS_EXPIRATION;

        return buildToken(claims, accountId, expirationTime);
    }

    /**
     * Generates a Refresh Token (7 Days)
     */
    public String generateRefreshToken(String accountId) {
        return buildToken(new HashMap<>(), accountId, REFRESH_TOKEN_EXPIRATION);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Created (iat)
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Exp (exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            // We only want to ensure it's not expired and has a subject
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
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