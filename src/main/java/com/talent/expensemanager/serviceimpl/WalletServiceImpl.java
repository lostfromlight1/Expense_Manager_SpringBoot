package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.exceptions.WalletException;
import com.talent.expensemanager.model.Account;
import com.talent.expensemanager.model.MyWallet;
import com.talent.expensemanager.repository.AccountRepository;
import com.talent.expensemanager.repository.WalletRepository;
import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.WalletResponse;
import com.talent.expensemanager.service.AuditService;
import com.talent.expensemanager.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public WalletResponse createWallet(WalletRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new WalletException("Account not found with ID: " + request.getAccountId()));

        if (walletRepository.findByAccount(account).isPresent()) {
            throw new WalletException("Account already has an active wallet.");
        }

        MyWallet wallet = new MyWallet();
        wallet.setWalletId("WAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        wallet.setAccount(account);
        wallet.setBalance(request.getBalance() != null ? request.getBalance() : 0.0);
        wallet.setBudget(request.getBudget() != null ? request.getBudget() : 0.0);
        wallet.setActive(true);

        walletRepository.save(wallet);

        auditService.log(
                "CREATE_WALLET",
                "Wallet",
                wallet.getWalletId(),
                "New wallet created via registration",
                account.getAccountId()
        );

        return mapToResponse(wallet);
    }

    @Override
    public WalletResponse getByAccountId(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new WalletException("Account not found"));

        MyWallet wallet = walletRepository.findByAccount(account)
                .orElseThrow(() -> new WalletException("No wallet found for this account"));

        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse updateBalance(String walletId, Double amount, boolean isIncrement) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletException("Wallet not found"));

        double newBalance = isIncrement ? wallet.getBalance() + amount : wallet.getBalance() - amount;
        wallet.setBalance(newBalance);

        walletRepository.save(wallet);

        auditService.log(
                "UPDATE_BALANCE",
                "Wallet",
                walletId,
                (isIncrement ? "Income" : "Expense") + " of " + amount,
                wallet.getAccount().getAccountId()
        );

        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse updateBudget(String walletId, Double newBudget) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletException("Wallet not found"));

        wallet.setBudget(newBudget);
        walletRepository.save(wallet);

        auditService.log(
                "UPDATE_BUDGET",
                "Wallet",
                walletId,
                "Budget updated to " + newBudget,
                wallet.getAccount().getAccountId()
        );

        return mapToResponse(wallet);
    }

    @Override
    public WalletResponse getByWalletId(String walletId) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletException("Wallet not found"));

        if (!wallet.isActive()) {
            throw new WalletException("Wallet is deactivated");
        }

        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    public void deleteWallet(String walletId) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletException("Wallet not found"));

        wallet.setActive(false);
        walletRepository.save(wallet);

        auditService.log(
                "DELETE_WALLET",
                "Wallet",
                walletId,
                "Wallet deactivated",
                wallet.getAccount().getAccountId()
        );
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