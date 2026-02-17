package com.talent.expensemanager.controller;

import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.response.WalletResponse;
import com.talent.expensemanager.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<BaseResponse<WalletResponse>> create(@RequestBody WalletRequest request) {
        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .success(true)
                .message("Wallet created successfully")
                .data(walletService.createWallet(request))
                .build());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<BaseResponse<WalletResponse>> getByAccountId(@PathVariable String accountId) {
        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .success(true)
                .message("Wallet synced")
                .data(walletService.getByAccountId(accountId))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<WalletResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .success(true)
                .message("Wallet retrieved successfully")
                .data(walletService.getByWalletId(id))
                .build());
    }

    @PutMapping("/{id}/budget")
    public ResponseEntity<BaseResponse<WalletResponse>> updateBudget(
            @PathVariable String id,
            @RequestBody WalletRequest request) {
        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .success(true)
                .message("Budget updated")
                .data(walletService.updateBudget(id, request.getBudget()))
                .build());
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<BaseResponse<WalletResponse>> updateBalance(
            @PathVariable String id,
            @RequestParam boolean isIncrement,
            @RequestBody WalletRequest request) {
        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .success(true)
                .message(isIncrement ? "Income added" : "Expense deducted")
                .data(walletService.updateBalance(id, request.getBalance(), isIncrement))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        walletService.deleteWallet(id);
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .success(true)
                .message("Wallet deactivated successfully")
                .build());
    }
}