package com.talent.expensemanager.model.enums;

import lombok.Getter;

@Getter
public enum CategoryType {
    // INCOME
    SALARY(TransactionType.INCOME),
    BUSINESS(TransactionType.INCOME),
    GIFT(TransactionType.INCOME),
    INVESTMENT(TransactionType.INCOME),

    // EXPENSE
    FOOD(TransactionType.EXPENSE),
    TRANSPORT(TransactionType.EXPENSE),
    RENT(TransactionType.EXPENSE),
    SHOPPING(TransactionType.EXPENSE),
    MEDICINE(TransactionType.EXPENSE),
    BILLS(TransactionType.EXPENSE);

    private final TransactionType type;

    CategoryType(TransactionType type) {
        this.type = type;
    }
}