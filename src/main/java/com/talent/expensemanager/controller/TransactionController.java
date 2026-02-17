package com.talent.expensemanager.controller;

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
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<BaseResponse<TransactionResponse>> create(@RequestBody TransactionRequest request) {
        return ResponseEntity.ok(BaseResponse.<TransactionResponse>builder()
                .success(true)
                .message("Transaction recorded")
                .data(transactionService.createTransaction(request))
                .build());
    }

    @GetMapping("/summary/{walletId}")
    public ResponseEntity<BaseResponse<MonthlyOverviewResponse>> getSummary(@PathVariable String walletId) {
        return ResponseEntity.ok(BaseResponse.<MonthlyOverviewResponse>builder()
                .success(true)
                .data(transactionService.getMonthlySummary(walletId))
                .build());
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<BaseResponse<List<TransactionResponse>>> getByWallet(@PathVariable String walletId) {
        return ResponseEntity.ok(BaseResponse.<List<TransactionResponse>>builder()
                .success(true)
                .data(transactionService.getTransactionsByWalletId(walletId))
                .build());
    }

    @GetMapping("/wallet/{walletId}/range")
    public ResponseEntity<BaseResponse<List<TransactionResponse>>> getByRange(
            @PathVariable String walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(BaseResponse.<List<TransactionResponse>>builder()
                .success(true)
                .data(transactionService.getTransactionsByRange(walletId, start, end))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionResponse>> update(@PathVariable String id, @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(BaseResponse.<TransactionResponse>builder()
                .success(true)
                .message("Transaction updated")
                .data(transactionService.updateTransaction(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .success(true)
                .message("Transaction deleted")
                .build());
    }
}