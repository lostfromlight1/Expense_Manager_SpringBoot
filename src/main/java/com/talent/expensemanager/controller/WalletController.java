package com.talent.expensemanager.controller;

import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.response.WalletResponse;
import com.talent.expensemanager.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #request.accountId == authentication.principal")
    public ResponseEntity<BaseResponse<WalletResponse>> create(@RequestBody WalletRequest request) {
        LOGGER.info("REST request to create wallet for account: {}", request.getAccountId());

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
        LOGGER.info("REST request by ADMIN to fetch all system wallets");

        List<WalletResponse> response = walletService.getAllWallets();

        return ResponseEntity.ok(BaseResponse.<List<WalletResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getAllWallets")
                .apiId("wallet-get-all")
                .message("All wallet records retrieved successfully.")
                .data(response)
                .build());
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or #accountId == authentication.principal")
    public ResponseEntity<BaseResponse<WalletResponse>> getByAccountId(@PathVariable String accountId) {
        LOGGER.info("REST request to get wallet for account ID: {}", accountId);

        WalletResponse response = walletService.getByAccountId(accountId);

        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getWalletByAccount")
                .apiId("wallet-account-get")
                .message("Wallet synced")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @walletSecurity.isWalletOwner(#id)")
    public ResponseEntity<BaseResponse<WalletResponse>> get(@PathVariable String id) {
        LOGGER.info("REST request to get wallet details for ID: {}", id);

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
    @PreAuthorize("hasRole('ADMIN') or @walletSecurity.isWalletOwner(#id)")
    public ResponseEntity<BaseResponse<WalletResponse>> updateBudget(
            @PathVariable String id,
            @RequestBody WalletRequest request) {
        LOGGER.info("REST request to update budget for wallet: {} to {}", id, request.getBudget());

        WalletResponse response = walletService.updateBudget(id, request.getBudget());

        LOGGER.info("Budget updated successfully for wallet: {}", id);
        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("updateBudget")
                .apiId("budget-put")
                .message("Budget updated")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/balance")
    @PreAuthorize("hasRole('ADMIN') or @walletSecurity.isWalletOwner(#id)")
    public ResponseEntity<BaseResponse<WalletResponse>> updateBalance(
            @PathVariable String id,
            @RequestParam boolean isIncrement,
            @RequestBody WalletRequest request) {
        LOGGER.info("REST request to update balance for wallet: {} | Amount: {} | Increment: {}",
                id, request.getBalance(), isIncrement);

        WalletResponse response = walletService.updateBalance(id, request.getBalance(), isIncrement);

        LOGGER.info("Balance successfully updated for wallet: {}. New Balance: {}", id, response.getBalance());
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
        LOGGER.info("REST request to deactivate wallet ID: {}", id);

        walletService.deleteWallet(id);

        LOGGER.info("Wallet {} successfully deactivated", id);
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("deleteWallet")
                .apiId("wallet-delete")
                .message("Wallet deactivated successfully")
                .build());
    }
}