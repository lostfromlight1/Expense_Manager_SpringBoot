package com.talent.expensemanager.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ApiKeyAuthentication extends AbstractAuthenticationToken {
    private final String accountId; // Renamed from apiKey for clarity

    public ApiKeyAuthentication(String accountId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.accountId = accountId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return accountId; // Returns the UUID of the user
    }
}