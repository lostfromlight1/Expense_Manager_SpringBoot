package com.talent.expensemanager.service;

import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.WalletResponse;

import java.util.List;

public interface WalletService {
    WalletResponse createWallet(WalletRequest request);

    WalletResponse updateBalance(String walletId, Double amount, boolean isIncrement);

    WalletResponse updateBudget(String walletId, Double newBudget);

    WalletResponse getByWalletId(String walletId);

    void deleteWallet(String walletId);

    WalletResponse getByAccountId(String accountId);

    List<WalletResponse> getAllWallets();
}