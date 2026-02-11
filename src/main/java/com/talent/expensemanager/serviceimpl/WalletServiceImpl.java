package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.model.Account;
import com.talent.expensemanager.model.MyWallet;
import com.talent.expensemanager.repository.AccountRepository;
import com.talent.expensemanager.repository.WalletRepository;
import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.WalletResponse;
import com.talent.expensemanager.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;

    @Override
    public WalletResponse createWallet(WalletRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        MyWallet wallet = new MyWallet();
        wallet.setWalletId("WAL-" + UUID.randomUUID().toString().substring(0, 8));

        wallet.setAccount(account);

        wallet.setBalance(request.getBalance());
        wallet.setBudget(request.getBudget());

        walletRepository.save(wallet);
        return mapToResponse(wallet);
    }

    @Override
    public WalletResponse updateBalanceAndBudget(String walletId, WalletRequest request) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(request.getBalance());
        wallet.setBudget(request.getBudget());

        walletRepository.save(wallet);
        return mapToResponse(wallet);
    }

    @Override
    public WalletResponse getByWalletId(String walletId) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!wallet.isActive()) {
            throw new RuntimeException("Wallet is deactivated");
        }

        return mapToResponse(wallet);
    }

    @Override
    public void deleteWallet(String walletId) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setActive(false);
        wallet.setDeletedDatetime(LocalDateTime.now());
        walletRepository.save(wallet);
    }

    private WalletResponse mapToResponse(MyWallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getWalletId())
                .accountId(wallet.getAccount().getAccountId())
                .balance(wallet.getBalance())
                .budget(wallet.getBudget())
                .build();
    }
}