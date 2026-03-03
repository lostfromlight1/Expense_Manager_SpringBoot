package com.talent.expensemanager.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.stream.Stream;

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

    @JsonCreator
    public static CategoryType fromString(String value) {
        return Stream.of(CategoryType.values())
                .filter(c -> c.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}