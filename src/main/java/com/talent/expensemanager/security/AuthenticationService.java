package com.talent.expensemanager.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ApiKeyConfiguration apiKeyConfiguration;
    private final JwtService jwtService;

    public Authentication doAuthentication(HttpServletRequest request) {
        String requestApiKey = request.getHeader("X-expense-api-key");
        // Using "token" header as per your request
        String token = request.getHeader("token");

        // 1. Verify App API Key
        if (requestApiKey == null || !apiKeyConfiguration.getApikey().equals(requestApiKey)) {
            throw new BadCredentialsException("Invalid or missing API Key");
        }

        // 2. Verify JWT Token exists
        if (token == null || token.isEmpty()) {
            throw new BadCredentialsException("Missing or invalid Authorization token");
        }

        try {
            // Extract data from the token
            String accountId = jwtService.extractAccountId(token);
            String roleName = jwtService.extractRole(token);

            // Validate that the token isn't expired
            if (!jwtService.isTokenValid(token)) {
                throw new BadCredentialsException("Token has expired");
            }

            var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));

            // Principal is the UUID/ID from the token
            return new ApiKeyAuthentication(accountId, authorities);
        } catch (Exception e) {
            throw new BadCredentialsException("Expired or invalid JWT");
        }
    }
}