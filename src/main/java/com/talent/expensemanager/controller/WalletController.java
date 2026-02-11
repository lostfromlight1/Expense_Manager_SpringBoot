package com.talent.expensemanager.controller;

import com.talent.expensemanager.request.WalletRequest;
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
    public ResponseEntity<WalletResponse> create(@RequestBody WalletRequest request) {
        return ResponseEntity.ok(walletService.createWallet(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(walletService.getByWalletId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WalletResponse> update(@PathVariable String id, @RequestBody WalletRequest request) {
        return ResponseEntity.ok(walletService.updateBalanceAndBudget(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }
}