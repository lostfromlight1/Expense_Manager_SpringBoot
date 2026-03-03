package com.talent.expensemanager.service;

import com.talent.expensemanager.model.enums.TransactionType;
import com.talent.expensemanager.request.TransactionRequest;
import com.talent.expensemanager.response.MonthlyOverviewResponse;
import com.talent.expensemanager.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TransactionService {
    TransactionResponse createTransaction(TransactionRequest request);
    TransactionResponse updateTransaction(String id, TransactionRequest request);
    void deleteTransaction(String transactionId);

    Page<TransactionResponse> searchTransactions(String walletId, TransactionType type,
                                                 LocalDateTime start, LocalDateTime end, Pageable pageable);

    MonthlyOverviewResponse getMonthlyOverview(String walletId, Integer month, Integer year);
    Page<TransactionResponse> getAllTransactions(Pageable pageable);
}