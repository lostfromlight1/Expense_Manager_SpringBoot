package com.talent.expensemanager.controller;

import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.response.WalletResponse;
import com.talent.expensemanager.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;


    @GetMapping("/me")
    public ResponseEntity<BaseResponse<WalletResponse>> getMyWallet(Authentication authentication) {
        String currentUserId = (String) authentication.getPrincipal();
        LOGGER.info("User {} fetching their own wallet", currentUserId);

        WalletResponse response = walletService.getByAccountId(currentUserId);

        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getMyWallet")
                .apiId("wallet-me-get")
                .message("Wallet synced successfully")
                .data(response)
                .build());
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("@permissionSecurity.hasAccountAccess(#accountId) or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<WalletResponse>> getByAccount(@PathVariable String accountId) {
        LOGGER.info("Fetching wallet for Account ID: {}", accountId);

        WalletResponse response = walletService.getByAccountId(accountId);

        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getWalletByAccount")
                .apiId("wallet-account-get")
                .message("Wallet retrieved by account ID")
                .data(response)
                .build());
    }

    @PostMapping
    @PreAuthorize("@permissionSecurity.hasAccountAccess(#request.accountId)")
    public ResponseEntity<BaseResponse<WalletResponse>> create(@Valid @RequestBody WalletRequest request) {
        LOGGER.info("Creating wallet for account: {}", request.getAccountId());
        WalletResponse response = walletService.createWallet(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<WalletResponse>builder()
                .httpStatusCode(HttpStatus.CREATED.value())
                .apiName("createWallet")
                .apiId("wallet-post")
                .message("Wallet created successfully")
                .data(response)
                .build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<WalletResponse>>> getAll() {
        LOGGER.info("Admin fetching all system wallets");
        List<WalletResponse> response = walletService.getAllWallets();

        return ResponseEntity.ok(BaseResponse.<List<WalletResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getAllWallets")
                .apiId("wallet-get-all")
                .message("All wallet records retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#id)")
    public ResponseEntity<BaseResponse<WalletResponse>> get(@PathVariable String id) {
        LOGGER.info("Fetching wallet details for ID: {}", id);
        WalletResponse response = walletService.getByWalletId(id);

        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getWallet")
                .apiId("wallet-get")
                .message("Wallet retrieved successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/budget")
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#id)")
    public ResponseEntity<BaseResponse<WalletResponse>> updateBudget(
            @PathVariable String id,
            @RequestBody WalletRequest request) {
        LOGGER.info("Updating budget for wallet: {} to {}", id, request.getBudget());
        WalletResponse response = walletService.updateBudget(id, request.getBudget());

        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("updateBudget")
                .apiId("budget-put")
                .message("Budget updated successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/balance")
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#id)")
    public ResponseEntity<BaseResponse<WalletResponse>> updateBalance(
            @PathVariable String id,
            @RequestParam boolean isIncrement,
            @Valid @RequestBody WalletRequest request) {
        LOGGER.info("Updating balance for wallet: {} | amount: {} | increment: {}", id, request.getBalance(), isIncrement);
        WalletResponse response = walletService.updateBalance(id, request.getBalance(), isIncrement);

        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("updateBalance")
                .apiId("balance-put")
                .message(isIncrement ? "Income added" : "Expense deducted")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        LOGGER.info("Deactivating wallet ID: {}", id);
        walletService.deleteWallet(id);

        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("deleteWallet")
                .apiId("wallet-delete")
                .message("Wallet deactivated successfully")
                .build());
    }
}