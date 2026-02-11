package com.talent.expensemanager.request;

import lombok.Data;

@Data
public class WalletRequest {
    private String accountId;
    private double balance;
    private double budget;
}