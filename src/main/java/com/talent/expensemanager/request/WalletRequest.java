package com.talent.expensemanager.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class WalletRequest {
    @NotBlank(message = "Account ID is required to link the wallet")
    private String accountId;

    @NotNull(message = "Initial balance is required")
    @Min(value = 0, message = "Initial balance cannot be negative")
    private Double balance;

    @NotNull(message = "Monthly budget is required")
    @PositiveOrZero(message = "Budget cannot be negative")
    private Double budget;
}