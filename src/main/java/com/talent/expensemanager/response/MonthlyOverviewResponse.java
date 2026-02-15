package com.talent.expensemanager.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyOverviewResponse {
    private double totalIncome;
    private double totalExpense;
    private double currentBalance;
    private double monthlyBudget;
    private double remainingBudget;
    private boolean isBudgetExceeded;
}