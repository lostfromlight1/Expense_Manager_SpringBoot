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
        if (auth == null || Objects.equals(auth.getPrincipal(), "anonymousUser")) return null;
        return (String) auth.getPrincipal();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
    }

    public boolean isAccountOwner(String accountId) {
        String currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(accountId);
    }

    public boolean hasAccountAccess(String accountId) {
        if (isAdmin()) return true;
        return isAccountOwner(accountId);
    }

    public boolean hasWalletAccess(String walletId) {
        if (isAdmin()) return true;
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;

        return walletRepository.findById(walletId)
                .map(w -> w.getAccount().getAccountId().equals(currentUserId))
                .orElse(false);
    }

    public boolean hasTransactionAccess(String transactionId) {
        if (isAdmin()) return true;
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;

        return transactionRepository.findById(transactionId)
                .map(t -> t.getWallet().getAccount().getAccountId().equals(currentUserId))
                .orElse(false);
    }
}