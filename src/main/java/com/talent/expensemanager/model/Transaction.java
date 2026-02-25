package com.talent.expensemanager.model;

import com.talent.expensemanager.model.enums.CategoryType;
import com.talent.expensemanager.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "transactions")
public class Transaction extends AbstractEntity {

    @Id
    @Column(name = "transaction_id", length = 50)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private MyWallet wallet;

    @Column(name = "transaction_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private CategoryType categoryType;

    @Column(name = "description")
    private String description;
    
    private double amount;
}