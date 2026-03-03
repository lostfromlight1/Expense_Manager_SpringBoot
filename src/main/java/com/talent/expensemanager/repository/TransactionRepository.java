package com.talent.expensemanager.repository;

import com.talent.expensemanager.model.Transaction;
import com.talent.expensemanager.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String>,
        JpaSpecificationExecutor<Transaction> {

    Page<Transaction> findByWallet_WalletIdAndActiveTrue(String walletId, Pageable pageable);

    List<Transaction> findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(String walletId, LocalDateTime start, LocalDateTime end);

    Page<Transaction> findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(
            String walletId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Transaction> findByWallet_WalletIdAndTransactionTypeAndActiveTrue(
            String walletId, TransactionType type, Pageable pageable);

    @Override
    @NonNull
    Page<Transaction> findAll(@NonNull Pageable pageable);
}