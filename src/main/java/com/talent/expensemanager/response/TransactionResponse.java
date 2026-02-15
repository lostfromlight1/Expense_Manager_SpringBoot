package com.talent.expensemanager.response;

import com.talent.expensemanager.model.enums.CategoryType;
import com.talent.expensemanager.model.enums.TransactionType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String transactionId;
    private String walletId;
    private TransactionType transactionType;
    private CategoryType categoryType;
    private double amount;
    private double walletBalanceAfterTransaction;
    private LocalDateTime createdAt;
}