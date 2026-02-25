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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        MyWallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new TransactionException("Wallet not found"));

        String role = wallet.getAccount().getRole().getName();
        LOGGER.info("createTransaction {} : {} is started now.", role, wallet.getWalletId());

        Transaction t = new Transaction();
        t.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        t.setWallet(wallet);
        t.setTransactionType(request.getTransactionType());
        t.setCategoryType(request.getCategoryType());
        t.setAmount(request.getAmount());
        t.setDescription(request.getDescription()); // Requires 'description' field in Transaction model
        t.setActive(true);

        if (t.getTransactionType() == TransactionType.INCOME) {
            wallet.setBalance(wallet.getBalance() + t.getAmount());
        } else {
            wallet.setBalance(wallet.getBalance() - t.getAmount());
        }

        Transaction saved = transactionRepository.save(t);
        walletRepository.save(wallet);

        auditService.log("TRANSACTION", "Wallet", wallet.getWalletId(),
                t.getTransactionType() + ": " + t.getAmount(), wallet.getAccount().getAccountId());

        LOGGER.info("\ncreateTransaction {} : {} ALL SUCCESS", role, saved.getTransactionId());
        return mapToResponse(saved);
    }

    @Override
    public TransactionResponse getByTransactionId(String id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionException("Transaction not found"));
        return mapToResponse(t);
    }

    @Override
    public List<TransactionResponse> getTransactionsByType(String walletId, TransactionType type) {
        return transactionRepository.findByWallet_WalletIdAndTransactionTypeAndActiveTrue(walletId, type)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTransaction(String id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionException("Transaction not found"));

        MyWallet wallet = t.getWallet();
        String role = wallet.getAccount().getRole().getName();
        LOGGER.info("deleteTransaction {} : {} is started now.", role, id);

        if (t.getTransactionType() == TransactionType.INCOME) {
            wallet.setBalance(wallet.getBalance() - t.getAmount());
        } else {
            wallet.setBalance(wallet.getBalance() + t.getAmount());
        }

        t.setActive(false);
        transactionRepository.save(t);
        walletRepository.save(wallet);

        auditService.log("DELETE_TXN", "Transaction", id, "Transaction reversed", wallet.getAccount().getAccountId());
        LOGGER.info("\ndeleteTransaction {} : {} ALL SUCCESS", role, id);
    }

    @Override
    public MonthlyOverviewResponse getMonthlyOverview(String walletId, Integer month, Integer year) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new TransactionException("Wallet not found"));

        LOGGER.info("getMonthlyOverview {} : {} started.", wallet.getAccount().getRole(), walletId);

        LocalDateTime now = LocalDateTime.now();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        int targetYear = (year != null) ? year : now.getYear();

        LocalDateTime start = LocalDateTime.of(targetYear, targetMonth, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        List<Transaction> transactions = transactionRepository.findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(walletId, start, end);

        double totalIncome = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();

        double totalExpense = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();

        return MonthlyOverviewResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .currentBalance(wallet.getBalance())
                .monthlyBudget(wallet.getBudget())
                .remainingBudget(wallet.getBudget() - totalExpense)
                .isBudgetExceeded(totalExpense > wallet.getBudget())
                .build();
    }

    @Override
    public List<TransactionResponse> getTransactionsByWalletId(String walletId) {
        return transactionRepository.findByWallet_WalletIdAndActiveTrue(walletId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByRange(String walletId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(walletId, start, end)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(String id, TransactionRequest request) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionException("Transaction not found"));
        t.setDescription(request.getDescription());
        t.setCategoryType(request.getCategoryType());
        return mapToResponse(transactionRepository.save(t));
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