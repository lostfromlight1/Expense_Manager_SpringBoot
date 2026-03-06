package com.talent.expensemanager.request;

import com.talent.expensemanager.model.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransactionRequest {
    @NotBlank(message = "Wallet ID is required")
    private String walletId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}