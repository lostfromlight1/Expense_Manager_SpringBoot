package com.talent.expensemanager.controller;

import com.talent.expensemanager.exceptions.AccountException;
import com.talent.expensemanager.request.AccountRequest;
import com.talent.expensemanager.request.LoginRequest;
import com.talent.expensemanager.request.PasswordChangeRequest;
import com.talent.expensemanager.response.AccountResponse;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.security.JwtService;
import com.talent.expensemanager.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AccountResponse>> register(@RequestBody AccountRequest request) {
        LOGGER.info("REST request to register account for email: {}", request.getEmail());

        AccountResponse response = accountService.register(request);

        LOGGER.info("Account successfully registered with ID: {}", response.getAccountId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<AccountResponse>builder()
                .httpStatusCode(HttpStatus.CREATED.value())
                .apiName("register")
                .apiId("register-post")
                .message("Account created successfully.")
                .data(response)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AccountResponse>> login(@RequestBody LoginRequest request) {
        LOGGER.info("REST request to login account: {}", request.getEmail());

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
    public ResponseEntity<BaseResponse<AccountResponse>> refresh(@RequestParam String refreshToken) {
        if (jwtService.validateRefreshToken(refreshToken)) {
            String accountId = jwtService.extractAccountId(refreshToken);

            // Fetch account to get name and role for the new Access Token
            AccountResponse account = accountService.getById(accountId);

            String newAccessToken = jwtService.generateToken(
                    account.getAccountId(),
                    account.getName(),
                    account.getRole()
            );

            return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                    .httpStatusCode(HttpStatus.OK.value())
                    .message("Token refreshed successfully")
                    .data(AccountResponse.builder()
                            .token(newAccessToken)
                            .refreshToken(refreshToken) // Keep using the same refresh token
                            .build())
                    .build());
        }
        throw new AccountException("Invalid or expired refresh token");
    }

    @GetMapping("/profile/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    public ResponseEntity<BaseResponse<AccountResponse>> getAccount(@PathVariable String id) {
        LOGGER.info("REST request to get profile for ID: {}", id);

        AccountResponse response = accountService.getById(id);

        LOGGER.info("Profile retrieved successfully for ID: {}", id);
        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getProfile")
                .apiId("profile-get")
                .message("Profile retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<AccountResponse>>> getAllAccounts() {
        LOGGER.info("REST request to fetch all user accounts by ADMIN");

        List<AccountResponse> users = accountService.getAllAccounts();

        return ResponseEntity.ok(BaseResponse.<List<AccountResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getAllUsers")
                .apiId("users-get-all")
                .message("All user records retrieved successfully.")
                .data(users)
                .build());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    public ResponseEntity<BaseResponse<AccountResponse>> updateAccount(
            @PathVariable String id,
            @RequestBody AccountRequest request) {
        LOGGER.info("REST request to update account: {}", id);

        AccountResponse response = accountService.updateAccount(id, request);

        LOGGER.info("Account updated successfully for ID: {}", id);
        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("updateProfile")
                .apiId("update-put")
                .message("Account updated successfully.")
                .data(response)
                .build());
    }

    @PutMapping("/change-password/{id}")
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @PathVariable String id,
            @RequestBody PasswordChangeRequest request) {
        LOGGER.info("REST request to change password for account ID: {}", id);

        accountService.changePassword(id, request.getOldPassword(), request.getNewPassword());

        LOGGER.info("Password changed successfully for account ID: {}", id);
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("changePassword")
                .apiId("password-put")
                .message("Password updated successfully.")
                .build());
    }

    @DeleteMapping("/terminate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteAccount(@PathVariable String id) {
        LOGGER.info("REST request to delete account ID: {}", id);

        accountService.deleteAccount(id);

        LOGGER.info("Account and associated data deleted for ID: {}", id);
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("deleteAccount")
                .apiId("terminate-delete")
                .message("Account and all associated data permanently deleted.")
                .build());
    }
}