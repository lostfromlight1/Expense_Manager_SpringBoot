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
        LOGGER.info("Starting creation of {} transaction for Wallet: {}", request.getTransactionType(), request.getWalletId());

        MyWallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> {
                    LOGGER.error("Transaction failed: Wallet {} not found", request.getWalletId());
                    return new TransactionException("Wallet not found");
                });

        Transaction t = new Transaction();
        t.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        t.setWallet(wallet);
        t.setTransactionType(request.getTransactionType());
        t.setCategoryType(request.getCategoryType());
        t.setAmount(request.getAmount());
        t.setDescription(request.getDescription());
        t.setActive(true);

        // Adjust Balance
        double oldBalance = wallet.getBalance();
        if (t.getTransactionType() == TransactionType.INCOME) {
            wallet.setBalance(oldBalance + t.getAmount());
        } else {
            wallet.setBalance(oldBalance - t.getAmount());
        }

        Transaction saved = transactionRepository.save(t);
        walletRepository.save(wallet);

        LOGGER.info("Transaction {} saved. Wallet balance updated: {} -> {}",
                saved.getTransactionId(), oldBalance, wallet.getBalance());

        auditService.log("TRANSACTION_CREATED", "Wallet", wallet.getWalletId(),
                t.getTransactionType() + ": " + t.getAmount(), wallet.getAccount().getAccountId());

        return mapToResponse(saved);
    }

    @Override
    public List<TransactionResponse> getTransactionsByType(String walletId, TransactionType type) {
        LOGGER.info("Fetching all {} transactions for Wallet: {}", type, walletId);
        return transactionRepository.findByWallet_WalletIdAndTransactionTypeAndActiveTrue(walletId, type)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getAllTransactions() {
        LOGGER.info("ADMIN ACTION: Fetching all transactions in the system");
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTransaction(String id) {
        LOGGER.info("Attempting to delete Transaction: {}", id);

        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionException("Transaction not found"));

        MyWallet wallet = t.getWallet();
        double oldBalance = wallet.getBalance();

        // Reverse balance
        if (t.getTransactionType() == TransactionType.INCOME) {
            wallet.setBalance(oldBalance - t.getAmount());
        } else {
            wallet.setBalance(oldBalance + t.getAmount());
        }

        t.setActive(false);
        transactionRepository.save(t);
        walletRepository.save(wallet);

        LOGGER.warn("Transaction {} deactivated. Balance reversed. New Balance: {}", id, wallet.getBalance());
        auditService.log("TRANSACTION_DELETED", "Transaction", id, "Reversed: " + t.getAmount(), wallet.getAccount().getAccountId());
    }

    @Override
    public MonthlyOverviewResponse getMonthlyOverview(String walletId, Integer month, Integer year) {
        LOGGER.info("Generating monthly summary for Wallet: {} (Month: {}, Year: {})", walletId, month, year);

        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new TransactionException("Wallet not found"));

        LocalDateTime now = LocalDateTime.now();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        int targetYear = (year != null) ? year : now.getYear();

        LocalDateTime start = LocalDateTime.of(targetYear, targetMonth, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        List<Transaction> transactions = transactionRepository.findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(walletId, start, end);

        double totalIncome = transactions.stream()
                .filter(tr -> tr.getTransactionType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();

        double totalExpense = transactions.stream()
                .filter(tr -> tr.getTransactionType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();

        LOGGER.info("Summary generated: Income: {}, Expense: {}", totalIncome, totalExpense);

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
        LOGGER.info("Fetching all active transactions for Wallet: {}", walletId);
        return transactionRepository.findByWallet_WalletIdAndActiveTrue(walletId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByRange(String walletId, LocalDateTime start, LocalDateTime end) {
        LOGGER.info("Filtering transactions for Wallet: {} between {} and {}", walletId, start, end);
        return transactionRepository.findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(walletId, start, end)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(String id, TransactionRequest request) {
        LOGGER.info("Updating metadata for Transaction: {}", id);
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionException("Transaction not found"));

        t.setDescription(request.getDescription());
        t.setCategoryType(request.getCategoryType());

        Transaction updated = transactionRepository.save(t);
        LOGGER.info("Transaction {} updated successfully", id);
        return mapToResponse(updated);
    }

    @Override
    public TransactionResponse getByTransactionId(String id) {
        return transactionRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new TransactionException("Transaction not found"));
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId())
                .walletId(t.getWallet().getWalletId())
                .transactionType(t.getTransactionType())
                .categoryType(t.getCategoryType())
                .amount(t.getAmount())
                .description(t.getDescription())
                .walletBalanceAfterTransaction(t.getWallet().getBalance())
                .createdAt(t.getCreatedDatetime())
                .build();
    }
}