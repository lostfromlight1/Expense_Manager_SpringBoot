package com.talent.expensemanager.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ApiKeyConfiguration apiKeyConfiguration;
    private final JwtService jwtService;

    public Authentication doAuthentication(HttpServletRequest request) {
        String requestApiKey = request.getHeader("X-expense-api-key");
        String token = request.getHeader("token");
        String path = request.getServletPath();

        if (requestApiKey == null || !apiKeyConfiguration.getApikey().equals(requestApiKey)) {
            throw new BadCredentialsException("Invalid or missing API Key");
        }

        List<String> apiKeyOnlyRoutes = List.of(
                "/api/v1/accounts/register",
                "/api/v1/accounts/login",
                "/api/v1/accounts/refresh-token"
        );

        if (apiKeyOnlyRoutes.contains(path)) {
            return new ApiKeyAuthentication("PUBLIC_USER",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_PUBLIC")));
        }

        if (token == null || token.isEmpty()) {
            throw new BadCredentialsException("Missing or invalid Authorization token");
        }

        try {
            String accountId = jwtService.extractAccountId(token);
            String roleName = jwtService.extractRole(token);

            if (!jwtService.isTokenValid(token)) {
                throw new BadCredentialsException("Token has expired");
            }

            var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
            return new ApiKeyAuthentication(accountId, authorities);
        } catch (Exception e) {
            throw new BadCredentialsException("Expired or invalid JWT");
        }
    }
}