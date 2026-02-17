package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.model.Account;
import com.talent.expensemanager.repository.AccountRepository;
import com.talent.expensemanager.request.AccountRequest;
import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.AccountResponse;
import com.talent.expensemanager.service.AccountService;
import com.talent.expensemanager.service.AuditService;
import com.talent.expensemanager.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    @Override
    public AccountResponse register(AccountRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Account account = new Account();
        account.setAccountId(UUID.randomUUID().toString());
        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setDateOfBirth(String.valueOf(request.getDateOfBirth()));
        account.setActive(true);

        Account savedAccount = accountRepository.save(account);

        WalletRequest walletRequest = new WalletRequest();
        walletRequest.setAccountId(savedAccount.getAccountId());
        walletRequest.setBalance(0.0);
        walletRequest.setBudget(0.0);
        walletService.createWallet(walletRequest);

        auditService.log("REGISTER", "Account", savedAccount.getAccountId(),
                "User registered and wallet initialized", savedAccount.getAccountId());

        return mapToResponse(savedAccount);
    }

    @Override
    public AccountResponse login(String email, String password) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        auditService.log("LOGIN", "Account", account.getAccountId(),
                "User logged in successfully", account.getAccountId());

        return mapToResponse(account);
    }

    @Override
    public AccountResponse getById(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return mapToResponse(account);
    }

    @Override
    public AccountResponse updateAccount(String id, AccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setName(request.getName());
        account.setDateOfBirth(String.valueOf(request.getDateOfBirth()));
        account.setActive(request.isActive());

        accountRepository.save(account);

        auditService.log("UPDATE_PROFILE", "Account", id,
                "Profile details updated. Active: " + request.isActive(), id);

        return mapToResponse(account);
    }

    @Override
    public void changePassword(String id, String oldPassword, String newPassword) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new RuntimeException("The old password you entered is incorrect.");
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        auditService.log("CHANGE_PASSWORD", "Account", id, "User changed account password", id);
    }

    @Override
    public void deleteAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        auditService.log("DELETE_ACCOUNT", "Account", id, "Account permanently deleted", id);

        accountRepository.delete(account);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .name(account.getName())
                .email(account.getEmail())
                .dateOfBirth(account.getDateOfBirth())
                .active(account.isActive())
                .build();
    }
}