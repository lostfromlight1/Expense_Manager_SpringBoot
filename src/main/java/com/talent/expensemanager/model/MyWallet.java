package com.talent.expensemanager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "wallets")
@Data
@EqualsAndHashCode(callSuper = true)
public class MyWallet extends AbstractEntity {

    @Id
    @Column(name = "wallet_id",  nullable = false, unique = true)
    private String walletId;

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "balance")
    private double balance;

    @Column(name = "budget")
    private double budget;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;
}