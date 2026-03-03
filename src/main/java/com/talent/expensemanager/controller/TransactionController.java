package com.talent.expensemanager.controller;

import com.talent.expensemanager.model.enums.TransactionType;
import com.talent.expensemanager.request.TransactionRequest;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.response.MonthlyOverviewResponse;
import com.talent.expensemanager.response.TransactionResponse;
import com.talent.expensemanager.service.TransactionService;
import com.talent.expensemanager.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#request.walletId)")
    public ResponseEntity<BaseResponse<TransactionResponse>> create(@Valid @RequestBody TransactionRequest request) {
        LOGGER.info("REST request to create transaction for wallet: {}", request.getWalletId());
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<TransactionResponse>builder()
                .httpStatusCode(HttpStatus.CREATED.value())
                .message("Transaction recorded successfully")
                .data(response)
                .build());
    }

    /**
     * Universal Search Endpoint: Handles filtering by Type, Date Range, or just WalletId.
     */
    @GetMapping("/wallet/{walletId}/search")
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#walletId)")
    public ResponseEntity<BaseResponse<Page<TransactionResponse>>> search(
            @PathVariable String walletId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(sort = "createdDatetime", direction = Sort.Direction.DESC) Pageable pageable) {

        LOGGER.info("REST request to search transactions for wallet: {}", walletId);
        Page<TransactionResponse> response = transactionService.searchTransactions(walletId, type, start, end, pageable);
        return ResponseEntity.ok(BaseResponse.<Page<TransactionResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .data(response)
                .build());
    }

    @GetMapping("/summary/{walletId}")
    @PreAuthorize("@permissionSecurity.hasWalletAccess(#walletId)")
    public ResponseEntity<BaseResponse<MonthlyOverviewResponse>> getSummary(
            @PathVariable String walletId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LOGGER.info("REST request for monthly summary - wallet: {}, month: {}, year: {}", walletId, month, year);
        MonthlyOverviewResponse response = transactionService.getMonthlyOverview(walletId, month, year);
        return ResponseEntity.ok(BaseResponse.<MonthlyOverviewResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionSecurity.hasTransactionAccess(#id)")
    public ResponseEntity<BaseResponse<TransactionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request) {
        LOGGER.info("REST request to update transaction: {}", id);
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(BaseResponse.<TransactionResponse>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionSecurity.hasTransactionAccess(#id)")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        LOGGER.info("REST request to delete transaction: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .message("Transaction deleted and balance reversed")
                .build());
    }

    @GetMapping("/me/latest")
    public ResponseEntity<BaseResponse<Page<TransactionResponse>>> getMyLatest(
            Authentication authentication,
            @PageableDefault(size = 5, sort = "createdDatetime", direction = Sort.Direction.DESC) Pageable pageable) {
        String currentUserId = (String) authentication.getPrincipal();
        String walletId = walletService.getByAccountId(currentUserId).getWalletId();
        Page<TransactionResponse> response = transactionService.searchTransactions(walletId, null, null, null, pageable);
        return ResponseEntity.ok(BaseResponse.<Page<TransactionResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .data(response)
                .build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Page<TransactionResponse>>> getAll(
            @PageableDefault(sort = "createdDatetime", direction = Sort.Direction.DESC) Pageable pageable) {
        LOGGER.info("ADMIN request to fetch all system transactions");
        Page<TransactionResponse> response = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(BaseResponse.<Page<TransactionResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .data(response)
                .build());
    }
}