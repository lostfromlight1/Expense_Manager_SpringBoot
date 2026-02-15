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

        // Business Logic: Adjust Balance
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
        walletRepository.save(wallet); // Save the updated balance

        auditService.log("TRANSACTION_CREATED", "Wallet", wallet.getWalletId(),
                String.format("%s of %.2f recorded", t.getTransactionType(), t.getAmount()));

        return mapToResponse(t);
    }

    @Override
    public MonthlyOverviewResponse getMonthlySummary(String walletId) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new TransactionException("Wallet not found"));

        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime end = LocalDateTime.now();

        List<Transaction> transactions = transactionRepository.findMonthlyTransactions(walletId, start, end);

        double income = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();

        double expense = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
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
    @Transactional
    public void deleteTransaction(String id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionException("Transaction not found"));

        if (!t.isActive()) throw new TransactionException("Transaction already deleted");

        MyWallet wallet = t.getWallet();

        // Reverse the balance impact
        if (t.getTransactionType() == TransactionType.INCOME) {
            wallet.setBalance(wallet.getBalance() - t.getAmount());
        } else {
            wallet.setBalance(wallet.getBalance() + t.getAmount());
        }

        t.setActive(false);
        transactionRepository.save(t);
        walletRepository.save(wallet);

        auditService.log("TRANSACTION_DELETED", "Transaction", id, "Balance reversed");
    }

    @Override
    public TransactionResponse getByTransactionId(String id) {
        return transactionRepository.findById(id)
                .filter(Transaction::isActive)
                .map(this::mapToResponse)
                .orElseThrow(() -> new TransactionException("Active transaction not found"));
    }

    @Override
    public List<TransactionResponse> getTransactionsByWalletId(String walletId) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getWallet().getWalletId().equals(walletId) && t.isActive())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByDateRange(String walletId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(walletId, start, end) // Updated this line
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByType(String walletId, TransactionType type) {
        return transactionRepository.findByWallet_WalletIdAndTransactionTypeAndActiveTrue(walletId, type)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByRange(String walletId, LocalDateTime start, LocalDateTime end) {
        return List.of();
    }

    @Override
    public TransactionResponse updateTransaction(String id, TransactionRequest request) {
        throw new TransactionException("Updates not allowed. Please delete and recreate the transaction.");
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