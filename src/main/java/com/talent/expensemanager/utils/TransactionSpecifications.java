package com.talent.expensemanager.utils;

import com.talent.expensemanager.model.Transaction;
import com.talent.expensemanager.model.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecifications {

    public static Specification<Transaction> buildSearchQuery(
            String walletId, TransactionType type, LocalDateTime start, LocalDateTime end) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("wallet").get("walletId"), walletId));

            predicates.add(cb.isTrue(root.get("active")));

            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }

            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdDatetime"), start));
            }

            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdDatetime"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}