package com.talent.expensemanager.controller;

import com.talent.expensemanager.request.TransactionRequest;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.response.MonthlyOverviewResponse;
import com.talent.expensemanager.response.TransactionResponse;
import com.talent.expensemanager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#request.walletId)")
    public ResponseEntity<BaseResponse<TransactionResponse>> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<TransactionResponse>builder()
                .httpStatusCode(HttpStatus.CREATED.value())
                .apiName("createTransaction")
                .apiId("txn-post")
                .message("Transaction recorded successfully")
                .data(transactionService.createTransaction(request))
                .build());
    }

    @GetMapping("/summary/{walletId}")
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#walletId)")
    public ResponseEntity<BaseResponse<MonthlyOverviewResponse>> getSummary(
            @PathVariable String walletId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(BaseResponse.<MonthlyOverviewResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getMonthlySummary")
                .apiId("txn-summary-get")
                .message("Monthly overview retrieved")
                .data(transactionService.getMonthlyOverview(walletId, month, year))
                .build());
    }

    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#walletId)")
    public ResponseEntity<BaseResponse<List<TransactionResponse>>> getByWallet(@PathVariable String walletId) {
        return ResponseEntity.ok(BaseResponse.<List<TransactionResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getTransactionsByWallet")
                .apiId("txn-wallet-get")
                .message("Transactions retrieved successfully")
                .data(transactionService.getTransactionsByWalletId(walletId))
                .build());
    }

    @GetMapping("/wallet/{walletId}/range")
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#walletId)")
    public ResponseEntity<BaseResponse<List<TransactionResponse>>> getByRange(
            @PathVariable String walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(BaseResponse.<List<TransactionResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getTransactionsByRange")
                .apiId("txn-range-get")
                .message("Filtered transactions retrieved")
                .data(transactionService.getTransactionsByRange(walletId, start, end))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionSecurity.hasTransactionAccess(#id)")
    public ResponseEntity<BaseResponse<TransactionResponse>> update(@PathVariable String id, @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(BaseResponse.<TransactionResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("updateTransaction")
                .apiId("txn-put")
                .message("Transaction updated successfully")
                .data(transactionService.updateTransaction(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionSecurity.hasTransactionAccess(#id)")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("deleteTransaction")
                .apiId("txn-delete")
                .message("Transaction deleted and balance adjusted")
                .build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<TransactionResponse>>> getAll() {
        return ResponseEntity.ok(BaseResponse.<List<TransactionResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getAllTransactions")
                .apiId("txn-all-get")
                .message("All system transactions retrieved")
                .data(transactionService.getAllTransactions())
                .build());
    }
}