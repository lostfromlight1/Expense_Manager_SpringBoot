package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.model.Account;
import com.talent.expensemanager.repository.AccountRepository;
import com.talent.expensemanager.request.AccountRequest;
import com.talent.expensemanager.response.AccountResponse;
import com.talent.expensemanager.service.AccountService;
import com.talent.expensemanager.service.AuditService;
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

    @Override
    public AccountResponse register(AccountRequest request) {
        // Your Repository has existsByEmail, let's use it
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

        accountRepository.save(account);

        auditService.log("REGISTER", "Account", account.getAccountId(), "User created");

        return mapToResponse(account);
    }

    @Override
    public AccountResponse login(String email, String password) {
        Account account = accountRepository.findByEmail(email)
                .filter(a -> a.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        auditService.log("LOGIN", "Account", account.getAccountId(), "User logged in");

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

        // Explicitly handle the active status for deactivation
        account.setActive(request.isActive());

        accountRepository.save(account);
        auditService.log("UPDATE", "Account", id, "Account updated. Active status: " + request.isActive());

        return mapToResponse(account);
    }

    @Override
    public void deleteAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        accountRepository.delete(account);
        auditService.log("DELETE", "Account", id, "Account deleted");
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