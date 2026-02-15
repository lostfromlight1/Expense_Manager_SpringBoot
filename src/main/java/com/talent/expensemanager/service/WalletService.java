package com.talent.expensemanager.service;

import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.WalletResponse;

public interface WalletService {
    WalletResponse createWallet(WalletRequest request);

    WalletResponse updateBalanceAndBudget(String walletId, WalletRequest request);

    WalletResponse getByWalletId(String walletId);

    void deleteWallet(String walletId);
}