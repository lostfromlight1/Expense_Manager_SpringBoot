package com.talent.expensemanager.response;

import com.talent.expensemanager.model.enums.TransactionType;
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
    private Long categoryId;
    private String categoryName;
    private Double amount;
    private String description;
    private Double walletBalanceAfterTransaction;
    private LocalDateTime createdAt;
}