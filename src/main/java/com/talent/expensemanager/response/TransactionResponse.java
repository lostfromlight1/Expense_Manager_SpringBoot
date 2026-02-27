package com.talent.expensemanager.response;

import com.talent.expensemanager.model.enums.TransactionType;
import com.talent.expensemanager.model.enums.CategoryType; // 1. Import the Enum
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String walletId;
    private TransactionType transactionType;
    private CategoryType categoryType; // 2. Change String to CategoryType
    private Double amount;
    private String description;
    private Double walletBalanceAfterTransaction;
    private LocalDateTime createdAt;
}