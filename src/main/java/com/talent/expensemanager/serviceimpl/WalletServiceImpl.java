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
        auditService.log("CREATE_WALLET", "Wallet", wallet.getWalletId(), "New wallet created");

        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse updateBalanceAndBudget(String walletId, WalletRequest request) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletException("Wallet not found"));

        if (!wallet.isActive()) {
            throw new WalletException("Cannot update: The wallet has been deleted");
        }

        if (request.getBalance() != null) wallet.setBalance(request.getBalance());
        if (request.getBudget() != null) wallet.setBudget(request.getBudget());

        walletRepository.save(wallet);
        auditService.log("UPDATE_WALLET", "Wallet", walletId, "Wallet parameters updated");

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

        auditService.log("DELETE_WALLET", "Wallet", walletId, "Wallet deactivated");
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