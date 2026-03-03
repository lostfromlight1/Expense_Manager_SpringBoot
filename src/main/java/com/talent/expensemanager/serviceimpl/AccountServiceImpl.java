package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.exceptions.AccountException;
import com.talent.expensemanager.exceptions.ResourceNotFoundException;
import com.talent.expensemanager.model.Account;
import com.talent.expensemanager.model.Role;
import com.talent.expensemanager.repository.AccountRepository;
import com.talent.expensemanager.repository.RoleRepository;
import com.talent.expensemanager.request.AccountRequest;
import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.AccountResponse;
import com.talent.expensemanager.security.ApiKeyAuthentication;
import com.talent.expensemanager.security.JwtService;
import com.talent.expensemanager.service.AccountService;
import com.talent.expensemanager.service.AuditService;
import com.talent.expensemanager.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final JwtService jwtService;

    @Override
    public AccountResponse register(AccountRequest request) {
        LOGGER.info("Registering new account for email: {}", request.getEmail());

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AccountException("An account with this email already exists.");
        }

        Account account = new Account();
        account.setAccountId(UUID.randomUUID().toString());
        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setDateOfBirth(String.valueOf(request.getDateOfBirth()));
        account.setActive(true);

        String roleName = request.getEmail().equalsIgnoreCase("admin@gmail.com") ? "ADMIN" : "USER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AccountException("Role not found"));
        account.setRole(role);

        Account savedAccount = accountRepository.save(account);

        String accessToken = jwtService.generateToken(
                savedAccount.getAccountId(),
                savedAccount.getName(),
                savedAccount.getRole().getName()
        );
        String refreshToken = jwtService.generateRefreshToken(savedAccount.getAccountId());

        var auth = new ApiKeyAuthentication(savedAccount.getAccountId(),
                List.of(new SimpleGrantedAuthority("ROLE_" + savedAccount.getRole().getName())));
        SecurityContextHolder.getContext().setAuthentication(auth);

        WalletRequest walletRequest = new WalletRequest();
        walletRequest.setAccountId(savedAccount.getAccountId());
        walletRequest.setBalance(0.0);
        walletRequest.setBudget(0.0);
        walletService.createWallet(walletRequest);

        auditService.log("REGISTER", "Account", savedAccount.getAccountId(),
                "User registered and wallet initialized", savedAccount.getAccountId());

        AccountResponse response = mapToResponse(savedAccount);
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);

        LOGGER.info("Successfully registered account: {}", savedAccount.getAccountId());
        return response;
    }

    @Override
    public AccountResponse login(String email, String password) {
        LOGGER.info("Attempting login for email: {}", email);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountException("Invalid credentials"));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new AccountException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(
                account.getAccountId(),
                account.getName(),
                account.getRole().getName()
        );
        String refreshToken = jwtService.generateRefreshToken(account.getAccountId());

        auditService.log("LOGIN", "Account", account.getAccountId(),
                "User logged in successfully", account.getAccountId());

        AccountResponse response = mapToResponse(account);
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);

        LOGGER.info("Login successful for account: {}", account.getAccountId());
        return response;
    }

    @Override
    public AccountResponse getById(String id) {
        LOGGER.info("Fetching account details for ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));

        return mapToResponse(account);
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        LOGGER.info("Fetching all user accounts for admin view");
        List<AccountResponse> accounts = accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        auditService.log("ADMIN_VIEW_ALL", "Account", "SYSTEM",
                "Admin viewed all user accounts", "ADMIN");

        return accounts;
    }

    @Override
    public AccountResponse updateAccount(String id, AccountRequest request) {
        LOGGER.info("Updating profile for account: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot update: Account not found with ID: " + id));;

        account.setName(request.getName());
        account.setDateOfBirth(String.valueOf(request.getDateOfBirth()));
        account.setActive(request.isActive());

        accountRepository.save(account);

        auditService.log("UPDATE_PROFILE", "Account", id,
                "Profile details updated. Active status: " + request.isActive(), id);

        LOGGER.info("Successfully updated account: {}", id);
        return mapToResponse(account);
    }

    @Override
    public void changePassword(String id, String oldPassword, String newPassword) {
        LOGGER.info("Updating password for account: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new AccountException("The old password you entered is incorrect.");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        auditService.log("CHANGE_PASSWORD", "Account", id, "User changed account password", id);
        LOGGER.info("Successfully changed password for account: {}", id);
    }

    @Override
    public void deleteAccount(String id) {
        LOGGER.info("Deleting account: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot delete: Account not found with ID: " + id));

        auditService.log("DELETE_ACCOUNT", "Account", id, "Account permanently deleted", id);

        accountRepository.delete(account);
        LOGGER.info("Successfully deleted account: {}", id);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .name(account.getName())
                .email(account.getEmail())
                .dateOfBirth(account.getDateOfBirth())
                .active(account.isActive())
                .role(account.getRole().getName())
                .build();
    }
}