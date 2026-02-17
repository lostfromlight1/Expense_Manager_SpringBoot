package com.talent.expensemanager.service;

import com.talent.expensemanager.model.enums.TransactionType;
import com.talent.expensemanager.request.TransactionRequest;
import com.talent.expensemanager.response.MonthlyOverviewResponse;
import com.talent.expensemanager.response.TransactionResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    TransactionResponse createTransaction(TransactionRequest request);

    TransactionResponse updateTransaction(String id, TransactionRequest request);

    TransactionResponse getByTransactionId(String id);

    List<TransactionResponse> getTransactionsByWalletId(String walletId);

    void deleteTransaction(String transactionId);

    List<TransactionResponse> getTransactionsByType(String walletId, TransactionType type);

    List<TransactionResponse> getTransactionsByRange(String walletId, LocalDateTime start, LocalDateTime end);

    MonthlyOverviewResponse getMonthlySummary(String walletId, Integer month, Integer year);
}