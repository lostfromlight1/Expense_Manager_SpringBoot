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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletServiceImpl.class);
    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    @Override
    public List<WalletResponse> getAllWallets() {
        LOGGER.info("getAllWallets SYSTEM : Admin fetching all wallets.");
        return walletRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WalletResponse createWallet(WalletRequest request) {
        LOGGER.info("createWallet SYSTEM : {} is started now.", request.getAccountId());

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new WalletException("Account not found with ID: " + request.getAccountId()));

        MyWallet wallet = new MyWallet();
        wallet.setWalletId("WLT-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        wallet.setAccount(account);
        wallet.setBalance(request.getBalance());
        wallet.setBudget(request.getBudget());
        wallet.setActive(true);

        MyWallet savedWallet = walletRepository.save(wallet);
        auditService.log("CREATE_WALLET", "Wallet", savedWallet.getWalletId(), "Wallet created", account.getAccountId());

        return mapToResponse(savedWallet);
    }

    @Override
    @Transactional
    public WalletResponse updateBalance(String walletId, Double amount, boolean isIncrement) {
        MyWallet wallet = findWalletAndValidateAccess(walletId);
        String role = wallet.getAccount().getRole().getName();
        LOGGER.info("updateBalance {} : {} is started now.", role, walletId);

        if (isIncrement) {
            wallet.setBalance(wallet.getBalance() + amount);
        } else {
            wallet.setBalance(wallet.getBalance() - amount);
        }

        auditService.log("UPDATE_BALANCE", "Wallet", walletId,
                (isIncrement ? "Income: " : "Expense: ") + amount, wallet.getAccount().getAccountId());

        return mapToResponse(walletRepository.save(wallet));
    }

    @Override
    @Transactional
    public WalletResponse updateBudget(String walletId, Double newBudget) {
        MyWallet wallet = findWalletAndValidateAccess(walletId);
        LOGGER.info("updateBudget : {} started.", walletId);

        wallet.setBudget(newBudget);
        auditService.log("UPDATE_BUDGET", "Wallet", walletId, "New budget: " + newBudget, wallet.getAccount().getAccountId());

        return mapToResponse(walletRepository.save(wallet));
    }

    @Override
    public WalletResponse getByWalletId(String walletId) {
        MyWallet wallet = findWalletAndValidateAccess(walletId);
        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    public void deleteWallet(String walletId) {
        MyWallet wallet = findWalletAndValidateAccess(walletId);
        wallet.setActive(false);
        walletRepository.save(wallet);
        auditService.log("DELETE_WALLET", "Wallet", walletId, "Wallet deactivated", wallet.getAccount().getAccountId());
    }

    @Override
    public WalletResponse getByAccountId(String accountId) {
        validateAccess(accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new WalletException("Account not found"));
        MyWallet wallet = walletRepository.findByAccount(account)
                .orElseThrow(() -> new WalletException("Wallet not found"));
        return mapToResponse(wallet);
    }

    // Helper to check ownership or Admin role
    private MyWallet findWalletAndValidateAccess(String walletId) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletException("Wallet not found"));
        validateAccess(wallet.getAccount().getAccountId());
        return wallet;
    }

    private void validateAccess(String resourceOwnerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new WalletException("Not authenticated");

        String currentUserId = (String) auth.getPrincipal();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN") ||
                        Objects.equals(a.getAuthority(), "ADMIN"));

        if (!isAdmin && !resourceOwnerId.equals(currentUserId)) {
            LOGGER.warn("Unauthorized access attempt: User {} tried to access resource of User {}", currentUserId, resourceOwnerId);
            throw new WalletException("Access Denied: You cannot access this wallet.");
        }
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