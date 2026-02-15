package com.talent.expensemanager.repository;

import com.talent.expensemanager.model.Transaction;
import com.talent.expensemanager.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // 1. Updated method name from CreatedAt to CreatedDatetime
    List<Transaction> findByWallet_WalletIdAndTransactionTypeAndActiveTrue(String walletId, TransactionType type);

    // 2. Updated method name from CreatedAtBetween to CreatedDatetimeBetween
    List<Transaction> findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(
            String walletId, LocalDateTime start, LocalDateTime end);

    // 3. Updated JPQL query string to use t.createdDatetime
    @Query("SELECT t FROM Transaction t WHERE t.wallet.walletId = :walletId " +
            "AND t.active = true AND t.createdDatetime >= :start AND t.createdDatetime <= :end")
    List<Transaction> findMonthlyTransactions(@Param("walletId") String walletId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
}