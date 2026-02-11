package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.model.Account;
import com.talent.expensemanager.repository.AccountRepository;
import com.talent.expensemanager.request.AccountRequest;
import com.talent.expensemanager.response.AccountResponse;
import com.talent.expensemanager.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AccountResponse register(AccountRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Account account = new Account();
        account.setAccountId("ACC-" + UUID.randomUUID().toString().substring(0, 6));
        account.setName(request.getName());
        account.setDateOfBirth(request.getDateOfBirth());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setActive(request.isActive());

        accountRepository.save(account);

        return new AccountResponse(
                account.getAccountId(),
                account.getName(),
                account.getEmail(),
                account.getDateOfBirth()
        );
    }

    @Override
    public AccountResponse login(String email, String password) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!account.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return new AccountResponse(account.getAccountId(), account.getName(), account.getEmail());
    }

    @Override
    public AccountResponse getById(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return new AccountResponse(account.getAccountId(), account.getName(), account.getEmail());
    }

    @Override
    public AccountResponse updateAccount(String id, AccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (request.getName() != null) account.setName(request.getName());

        accountRepository.save(account);
        return new AccountResponse(account.getAccountId(), account.getName(), account.getEmail());
    }

    @Override
    public void deleteAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setActive(false);
        account.setDeletedDatetime(LocalDateTime.now());

        accountRepository.save(account);
    }
}