package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.exceptions.ResourceNotFoundException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletServiceImpl.class);
    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    @Override
    public List<WalletResponse> getAllWallets() {
        return walletRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public WalletResponse createWallet(WalletRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot create wallet: Account not found with ID: " + request.getAccountId()));

        MyWallet wallet = new MyWallet();
        wallet.setWalletId("WLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        wallet.setAccount(account);
        wallet.setBalance(request.getBalance());
        wallet.setBudget(request.getBudget());
        wallet.setActive(true);

        MyWallet savedWallet = walletRepository.save(wallet);
        auditService.log("CREATE_WALLET", "Wallet", savedWallet.getWalletId(), "Initial balance: " + savedWallet.getBalance(), account.getAccountId());

        return mapToResponse(savedWallet);
    }

    @Override
    @Transactional
    public WalletResponse updateBalance(String walletId, Double amount, boolean isIncrement) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with ID: " + walletId));

        double oldBalance = wallet.getBalance();
        if (isIncrement) {
            wallet.setBalance(oldBalance + amount);
        } else {
            if (oldBalance < amount) {
                throw new WalletException("Insufficient funds in wallet.");
            }
            wallet.setBalance(oldBalance - amount);
        }

        MyWallet savedWallet = walletRepository.save(wallet);
        auditService.log("UPDATE_BALANCE", "Wallet", walletId, (isIncrement ? "Income: " : "Expense: ") + amount, wallet.getAccount().getAccountId());
        return mapToResponse(savedWallet);
    }

    @Override
    @Transactional
    public WalletResponse updateBudget(String walletId, Double newBudget) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with ID: " + walletId));

        wallet.setBudget(newBudget);
        MyWallet savedWallet = walletRepository.save(wallet);
        auditService.log("UPDATE_BUDGET", "Wallet", walletId, "New budget: " + newBudget, wallet.getAccount().getAccountId());
        return mapToResponse(savedWallet);
    }

    @Override
    public WalletResponse getByWalletId(String walletId) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with ID: " + walletId));
        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    public void deleteWallet(String walletId) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with ID: " + walletId));

        wallet.setActive(false);
        walletRepository.save(wallet);
        auditService.log("DELETE_WALLET", "Wallet", walletId, "Wallet deactivated", wallet.getAccount().getAccountId());
    }

    @Override
    public WalletResponse getByAccountId(String accountId) {
        MyWallet wallet = walletRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No wallet associated with Account ID: " + accountId));

        return mapToResponse(wallet);
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