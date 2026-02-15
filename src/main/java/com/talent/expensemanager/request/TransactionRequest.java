package com.talent.expensemanager.request;

import com.talent.expensemanager.model.enums.CategoryType;
import com.talent.expensemanager.model.enums.TransactionType;
import lombok.Data;

@Data
public class TransactionRequest {
    private String walletId;
    private TransactionType transactionType;
    private CategoryType categoryType;
    private Double amount;
}