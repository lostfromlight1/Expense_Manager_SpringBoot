package com.talent.expensemanager.controller;

import com.talent.expensemanager.exceptions.AccountException;
import com.talent.expensemanager.request.AccountRequest;
import com.talent.expensemanager.request.LoginRequest;
import com.talent.expensemanager.request.PasswordChangeRequest;
import com.talent.expensemanager.response.AccountResponse;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.security.JwtService;
import com.talent.expensemanager.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AccountResponse>> register(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.register(request);
        LOGGER.info("Successfully registered account for email: {}", request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<AccountResponse>builder()
                .httpStatusCode(HttpStatus.CREATED.value())
                .apiName("register")
                .apiId("register-post")
                .message("Account created successfully.")
                .data(response)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AccountResponse>> login(@Valid @RequestBody LoginRequest request) {
        AccountResponse response = accountService.login(request.getEmail(), request.getPassword());
        LOGGER.info("Login successful for Account ID: {}", response.getAccountId());

        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("sign-in")
                .apiId("sign-in-post")
                .message("Login successful.")
                .data(response)
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<BaseResponse<AccountResponse>> refresh(
            @RequestParam String accountId,
            @RequestParam String refreshToken) {

        if (jwtService.validateRefreshToken(refreshToken)) {
            String tokenAccountId = jwtService.extractAccountId(refreshToken);

            if (!tokenAccountId.equals(accountId)) {
                throw new AccountException("Security Alert: Token does not belong to this account.");
            }

            AccountResponse account = accountService.getById(accountId);
            String newAccessToken = jwtService.generateToken(account.getAccountId(), account.getName(), account.getRole());

            account.setToken(newAccessToken);
            account.setRefreshToken(refreshToken);

            LOGGER.info("Token refreshed for account: {}", accountId);

            return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                    .httpStatusCode(HttpStatus.OK.value())
                    .apiName("refreshToken")
                    .apiId("auth-refresh")
                    .message("Token refreshed successfully")
                    .data(account)
                    .build());
        }
        throw new AccountException("Session expired. Please login again.");
    }

    @GetMapping("/profile/{id}")
    @PreAuthorize("@permissionSecurity.hasAccountAccess(#id)")
    public ResponseEntity<BaseResponse<AccountResponse>> getAccount(@PathVariable String id) {
        AccountResponse response = accountService.getById(id);
        LOGGER.info("Retrieved profile for ID: {}", id);

        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getProfile")
                .apiId("profile-get")
                .message("Profile retrieved successfully.")
                .data(response)
                .build());
    }
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<AccountResponse>> getMyProfile(Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();

        LOGGER.info("Fetching self-profile for Account ID: {}", currentUserId);

        AccountResponse response = accountService.getById(currentUserId);

        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getMyProfile")
                .apiId("profile-me-get")
                .message("Current user profile retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> users = accountService.getAllAccounts();
        LOGGER.info("Admin fetched all user accounts");

        return ResponseEntity.ok(BaseResponse.<List<AccountResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getAllUsers")
                .apiId("users-get-all")
                .message("All user records retrieved successfully.")
                .data(users)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionSecurity.hasAccountAccess(#id)")
    public ResponseEntity<BaseResponse<AccountResponse>> updateAccount(
            @PathVariable String id,
            @RequestBody AccountRequest request) {

        AccountResponse response = accountService.updateAccount(id, request);
        LOGGER.info("Account updated: {}", id);

        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("updateProfile")
                .apiId("update-put")
                .message("Account updated successfully.")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("@permissionSecurity.isAccountOwner(#id)")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @PathVariable String id,
            @RequestBody PasswordChangeRequest request) {

        LOGGER.info("Password change initiated for account: {}", id);

        accountService.changePassword(id, request.getOldPassword(), request.getNewPassword());

        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("changePassword")
                .message("Password updated successfully.")
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteAccount(@PathVariable String id) {
        accountService.deleteAccount(id);
        LOGGER.info("Account permanently deleted: {}", id);

        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("deleteAccount")
                .apiId("terminate-delete")
                .message("Account and associated data permanently deleted.")
                .build());
    }
}