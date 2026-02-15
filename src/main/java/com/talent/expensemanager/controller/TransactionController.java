package com.talent.expensemanager.controller;

import com.talent.expensemanager.model.enums.TransactionType;
import com.talent.expensemanager.request.TransactionRequest;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.response.MonthlyOverviewResponse;
import com.talent.expensemanager.response.TransactionResponse;
import com.talent.expensemanager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<BaseResponse<TransactionResponse>> create(@RequestBody TransactionRequest request) {
        return ResponseEntity.ok(BaseResponse.<TransactionResponse>builder()
                .success(true)
                .message("Transaction recorded successfully")
                .data(transactionService.createTransaction(request))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(BaseResponse.<TransactionResponse>builder()
                .success(true)
                .message("Transaction found")
                .data(transactionService.getByTransactionId(id))
                .build());
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<BaseResponse<List<TransactionResponse>>> getByWallet(@PathVariable String walletId) {
        return ResponseEntity.ok(BaseResponse.<List<TransactionResponse>>builder()
                .success(true)
                .message("Wallet transactions retrieved")
                .data(transactionService.getTransactionsByWalletId(walletId))
                .build());
    }

    @GetMapping("/wallet/{walletId}/summary")
    public ResponseEntity<BaseResponse<MonthlyOverviewResponse>> getSummary(@PathVariable String walletId) {
        MonthlyOverviewResponse data = transactionService.getMonthlySummary(walletId);
        String status = data.isBudgetExceeded() ? "Alert: Monthly budget exceeded!" : "Summary retrieved";

        return ResponseEntity.ok(BaseResponse.<MonthlyOverviewResponse>builder()
                .success(true)
                .message(status)
                .data(data)
                .build());
    }

    @GetMapping("/wallet/{walletId}/range")
    public ResponseEntity<BaseResponse<List<TransactionResponse>>> getByRange(
            @PathVariable String walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(BaseResponse.<List<TransactionResponse>>builder()
                .success(true)
                .message("Transactions for the selected period")
                .data(transactionService.getTransactionsByRange(walletId, start, end))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .success(true)
                .message("Transaction deleted and balance adjusted")
                .build());
    }
}