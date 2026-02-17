package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.exceptions.TransactionException;
import com.talent.expensemanager.model.MyWallet;
import com.talent.expensemanager.model.Transaction;
import com.talent.expensemanager.model.enums.TransactionType;
import com.talent.expensemanager.repository.TransactionRepository;
import com.talent.expensemanager.repository.WalletRepository;
import com.talent.expensemanager.request.TransactionRequest;
import com.talent.expensemanager.response.MonthlyOverviewResponse;
import com.talent.expensemanager.response.TransactionResponse;
import com.talent.expensemanager.service.AuditService;
import com.talent.expensemanager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        MyWallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new TransactionException("Wallet not found"));

        if (request.getTransactionType() == TransactionType.INCOME) {
            wallet.setBalance(wallet.getBalance() + request.getAmount());
        } else {
            if (wallet.getBalance() < request.getAmount()) {
                throw new TransactionException("Insufficient funds for this expense.");
            }
            wallet.setBalance(wallet.getBalance() - request.getAmount());
        }

        Transaction t = new Transaction();
        t.setTransactionId("TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        t.setWallet(wallet);
        t.setTransactionType(request.getTransactionType());
        t.setCategoryType(request.getCategoryType());
        t.setAmount(request.getAmount());
        t.setActive(true);

        transactionRepository.save(t);
        walletRepository.save(wallet);

        auditService.log(
                "TRANSACTION_CREATED",
                "Wallet",
                wallet.getWalletId(),
                String.format("%s of %.2f recorded", t.getTransactionType(), t.getAmount()),
                wallet.getAccount().getAccountId()
        );

        return mapToResponse(t);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(String id, TransactionRequest request) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionException("Transaction not found"));

        double oldAmount = existingTransaction.getAmount();
        String oldCategory = existingTransaction.getCategoryType().toString();

        TransactionType type = existingTransaction.getTransactionType();
        MyWallet wallet = existingTransaction.getWallet();

        adjustWalletBalance(wallet, oldAmount, type, true);

        existingTransaction.setAmount(request.getAmount());
        existingTransaction.setCategoryType(request.getCategoryType());
        existingTransaction.setUpdatedDatetime(LocalDateTime.now());

        adjustWalletBalance(wallet, request.getAmount(), type, false);

        Transaction updated = transactionRepository.save(existingTransaction);
        walletRepository.save(wallet);

        String auditMessage = String.format(
                "Updated %s: Amount changed from %.2f to %.2f, Category from %s to %s",
                type, oldAmount, request.getAmount(), oldCategory, request.getCategoryType()
        );

        auditService.log(
                "TRANSACTION_UPDATED",
                "Transaction",
                id,
                auditMessage,
                wallet.getAccount().getAccountId()
        );

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTransaction(String id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionException("Transaction not found"));

        if (!t.isActive()) throw new TransactionException("Already deleted");

        adjustWalletBalance(t.getWallet(), t.getAmount(), t.getTransactionType(), true);

        t.setActive(false);
        transactionRepository.save(t);
        walletRepository.save(t.getWallet());

        auditService.log(
                "TRANSACTION_DELETED",
                "Transaction",
                id,
                "Balance reversed",
                t.getWallet().getAccount().getAccountId()
        );
    }

    private void adjustWalletBalance(MyWallet wallet, double amount, TransactionType type, boolean isReverse) {
        if (type == TransactionType.INCOME) {
            wallet.setBalance(isReverse ? wallet.getBalance() - amount : wallet.getBalance() + amount);
        } else {
            if (!isReverse && wallet.getBalance() < amount) {
                throw new TransactionException("Insufficient funds in wallet");
            }
            wallet.setBalance(isReverse ? wallet.getBalance() + amount : wallet.getBalance() - amount);
        }
    }

    @Override
    public MonthlyOverviewResponse getMonthlySummary(String walletId) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new TransactionException("Wallet not found"));

        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository.findMonthlyTransactions(walletId, start, end);

        double income = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.INCOME && t.isActive())
                .mapToDouble(Transaction::getAmount).sum();

        double expense = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE && t.isActive())
                .mapToDouble(Transaction::getAmount).sum();

        return MonthlyOverviewResponse.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .currentBalance(wallet.getBalance())
                .monthlyBudget(wallet.getBudget())
                .remainingBudget(wallet.getBudget() - expense)
                .isBudgetExceeded(expense > wallet.getBudget())
                .build();
    }

    @Override
    public List<TransactionResponse> getTransactionsByWalletId(String walletId) {
        return transactionRepository.findByWallet_WalletIdAndActiveTrue(walletId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse getByTransactionId(String id) {
        return transactionRepository.findById(id)
                .filter(Transaction::isActive)
                .map(this::mapToResponse)
                .orElseThrow(() -> new TransactionException("Active transaction not found"));
    }

    @Override
    public List<TransactionResponse> getTransactionsByType(String walletId, TransactionType type) {
        return transactionRepository.findByWallet_WalletIdAndTransactionTypeAndActiveTrue(walletId, type)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByRange(String walletId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(walletId, start, end)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId())
                .walletId(t.getWallet().getWalletId())
                .transactionType(t.getTransactionType())
                .categoryType(t.getCategoryType())
                .amount(t.getAmount())
                .walletBalanceAfterTransaction(t.getWallet().getBalance())
                .createdAt(t.getCreatedDatetime())
                .build();
    }
}