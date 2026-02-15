package com.talent.expensemanager.controller;

import com.talent.expensemanager.request.WalletRequest;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.response.WalletResponse;
import com.talent.expensemanager.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
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

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<WalletResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .success(true)
                .message("Wallet retrieved successfully")
                .data(walletService.getByWalletId(id))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<WalletResponse>> update(@PathVariable String id, @RequestBody WalletRequest request) {
        return ResponseEntity.ok(BaseResponse.<WalletResponse>builder()
                .success(true)
                .message("Wallet updated successfully")
                .data(walletService.updateBalanceAndBudget(id, request))
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