package com.talent.expensemanager.request;

import lombok.Data;

@Data
public class WalletRequest {
    private String accountId;
    private Double balance;
    private Double budget;
}