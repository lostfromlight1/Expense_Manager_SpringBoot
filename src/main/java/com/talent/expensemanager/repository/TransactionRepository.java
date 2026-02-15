package com.talent.expensemanager.repository;

import com.talent.expensemanager.model.MyWallet;
import com.talent.expensemanager.model.Transaction;
import com.talent.expensemanager.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // View by Type (Income/Expense)
    List<Transaction> findByWallet_WalletIdAndTransactionTypeAndActiveTrue(String walletId, TransactionType type);

    // View by Date Range
    List<Transaction> findByWallet_WalletIdAndCreatedAtBetweenAndActiveTrue(
            String walletId, LocalDateTime start, LocalDateTime end);

    // Monthly Data Fetch
    @Query("SELECT t FROM Transaction t WHERE t.wallet.walletId = :walletId " +
            "AND t.active = true AND t.createdAt >= :start AND t.createdAt <= :end")
    List<Transaction> findMonthlyTransactions(@Param("walletId") String walletId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
}