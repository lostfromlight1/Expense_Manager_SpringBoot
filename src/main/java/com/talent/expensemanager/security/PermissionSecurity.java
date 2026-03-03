package com.talent.expensemanager.security;

import com.talent.expensemanager.repository.TransactionRepository;
import com.talent.expensemanager.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("permissionSecurity")
@RequiredArgsConstructor
public class PermissionSecurity {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || Objects.equals(auth.getPrincipal(), "anonymousUser")) {
            return null;
        }
        return (String) auth.getPrincipal();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
    }

    public boolean isAccountOwner(String accountId) {
        return Objects.equals(getCurrentUserId(), accountId);
    }

    public boolean hasAccountAccess(String accountId) {
        return isAdmin() || isAccountOwner(accountId);
    }

    public boolean hasWalletAccess(String walletId) {
        if (isAdmin()) return true;
        String currentUserId = getCurrentUserId();
        if (currentUserId == null || walletId == null) return false;

        return walletRepository.findById(walletId)
                .map(w -> Objects.equals(w.getAccount().getAccountId(), currentUserId))
                .orElse(false);
    }

    public boolean hasTransactionAccess(String transactionId) {
        if (isAdmin()) return true;
        String currentUserId = getCurrentUserId();
        if (currentUserId == null || transactionId == null) return false;

        return transactionRepository.findById(transactionId)
                .map(t -> Objects.equals(t.getWallet().getAccount().getAccountId(), currentUserId))
                .orElse(false);
    }
}