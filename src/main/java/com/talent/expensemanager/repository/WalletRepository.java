package com.talent.expensemanager.repository;

import com.talent.expensemanager.model.Account;
import com.talent.expensemanager.model.MyWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<MyWallet, String> {
    Optional<MyWallet> findByAccount(Account account);
}