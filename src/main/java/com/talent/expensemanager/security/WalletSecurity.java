package com.talent.expensemanager.security;

import com.talent.expensemanager.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("walletSecurity")
@RequiredArgsConstructor
public class WalletSecurity {
    private final WalletRepository walletRepository;

    public boolean isWalletOwner(String walletId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        String currentUserId = (String) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ADMIN"));

        // Admin can access any wallet; User can only access if they own it
        if (isAdmin) return true;

        return walletRepository.findById(walletId)
                .map(w -> w.getAccount().getAccountId().equals(currentUserId))
                .orElse(false);
    }
}