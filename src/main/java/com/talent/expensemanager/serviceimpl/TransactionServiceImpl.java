package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.exceptions.ResourceNotFoundException;
import com.talent.expensemanager.exceptions.TransactionException;
import com.talent.expensemanager.model.Category;
import com.talent.expensemanager.model.MyWallet;
import com.talent.expensemanager.model.Transaction;
import com.talent.expensemanager.model.enums.TransactionType;
import com.talent.expensemanager.repository.TransactionRepository;
import com.talent.expensemanager.repository.WalletRepository;
import com.talent.expensemanager.repository.CategoryRepository;
import com.talent.expensemanager.request.TransactionRequest;
import com.talent.expensemanager.response.MonthlyOverviewResponse;
import com.talent.expensemanager.response.TransactionResponse;
import com.talent.expensemanager.service.AuditService;
import com.talent.expensemanager.service.TransactionService;
import com.talent.expensemanager.utils.TransactionSpecifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuditService auditService;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        LOGGER.info("Creating {} for wallet ID: {}", request.getTransactionType(), request.getWalletId());

        MyWallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getTransactionType() != request.getTransactionType()) {
            throw new TransactionException("Category '" + category.getName() + "' is invalid for " + request.getTransactionType());
        }

        Transaction t = new Transaction();
        t.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        t.setWallet(wallet);
        t.setTransactionType(request.getTransactionType());
        t.setCategory(category);
        t.setAmount(request.getAmount());
        t.setDescription(request.getDescription());
        t.setActive(true);

        double oldBalance = wallet.getBalance();
        if (t.getTransactionType() == TransactionType.INCOME) {
            wallet.setBalance(oldBalance + t.getAmount());
        } else {
            if (oldBalance < t.getAmount()) {
                LOGGER.warn("Balance insufficient for wallet: {}", wallet.getWalletId());
                throw new TransactionException("Insufficient wallet balance.");
            }
            wallet.setBalance(oldBalance - t.getAmount());
        }

        Transaction saved = transactionRepository.save(t);
        walletRepository.save(wallet);

        auditService.log("TRANSACTION_CREATED", "Wallet", wallet.getWalletId(),
                t.getTransactionType() + ": " + t.getAmount(), wallet.getAccount().getAccountId());

        return mapToResponse(saved);
    }

    @Override
    public Page<TransactionResponse> searchTransactions(String walletId, TransactionType type,
                                                        LocalDateTime start, LocalDateTime end, Pageable pageable) {
        LOGGER.debug("Dynamic search - Wallet: {}, Type: {}, Start: {}, End: {}", walletId, type, start, end);
        Specification<Transaction> spec = TransactionSpecifications.buildSearchQuery(walletId, type, start, end);
        return transactionRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public MonthlyOverviewResponse getMonthlyOverview(String walletId, Integer month, Integer year) {
        MyWallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        LocalDateTime now = LocalDateTime.now();
        int tMonth = (month != null) ? month : now.getMonthValue();
        int tYear = (year != null) ? year : now.getYear();

        LocalDateTime start = LocalDateTime.of(tYear, tMonth, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        List<Transaction> txns = transactionRepository.findByWallet_WalletIdAndCreatedDatetimeBetweenAndActiveTrue(walletId, start, end);

        double totalIncome = txns.stream().filter(tr -> tr.getTransactionType() == TransactionType.INCOME).mapToDouble(Transaction::getAmount).sum();
        double totalExpense = txns.stream().filter(tr -> tr.getTransactionType() == TransactionType.EXPENSE).mapToDouble(Transaction::getAmount).sum();

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
    @Transactional
    public TransactionResponse updateTransaction(String id, TransactionRequest request) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getTransactionType() != request.getTransactionType()) {
            throw new TransactionException("Category '" + category.getName() + "' is invalid for " + request.getTransactionType());
        }
        LOGGER.info("Updating transaction metadata for ID: {}", id);
        t.setDescription(request.getDescription());
        t.setCategory(category);

        Transaction updated = transactionRepository.save(t);

        auditService.log("TRANSACTION_UPDATED", "Transaction", id, "Metadata updated", t.getWallet().getAccount().getAccountId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTransaction(String id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        MyWallet wallet = t.getWallet();
        LOGGER.info("Soft-deleting transaction: {} and reversing amount: {}", id, t.getAmount());

        if (t.getTransactionType() == TransactionType.INCOME) {
            wallet.setBalance(wallet.getBalance() - t.getAmount());
        } else {
            wallet.setBalance(wallet.getBalance() + t.getAmount());
        }

        t.setActive(false);
        transactionRepository.save(t);
        walletRepository.save(wallet);

        auditService.log("TRANSACTION_DELETED", "Transaction", id, "Reversed: " + t.getAmount(), wallet.getAccount().getAccountId());
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId())
                .walletId(t.getWallet().getWalletId())
                .transactionType(t.getTransactionType())
                .categoryId(t.getCategory().getCategoryId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .walletBalanceAfterTransaction(t.getWallet().getBalance())
                .createdAt(t.getCreatedDatetime())
                .build();
    }
}