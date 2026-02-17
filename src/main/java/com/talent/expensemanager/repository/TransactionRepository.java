package com.talent.expensemanager.repository;

import com.talent.expensemanager.model.Transaction;
import com.talent.expensemanager.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByWallet_WalletIdAndActiveTrue(String walletId);

    List<Transaction> findByWallet_WalletIdAndTransactionTypeAndActiveTrue(String walletId, TransactionType type);

    List<Transaction> findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(String walletId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.wallet.walletId = ?1 AND t.createdDatetime BETWEEN ?2 AND ?3 AND t.active = true")
    List<Transaction> findMonthlyTransactions(String walletId, LocalDateTime start, LocalDateTime end);
}