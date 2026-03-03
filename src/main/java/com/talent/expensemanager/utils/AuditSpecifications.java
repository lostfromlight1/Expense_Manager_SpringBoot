package com.talent.expensemanager.utils;

import com.talent.expensemanager.model.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditSpecifications {

    public static Specification<AuditLog> buildSearchQuery(
            String action, String performedBy, LocalDateTime start, LocalDateTime end) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (action != null && !action.isEmpty()) {
                predicates.add(cb.equal(root.get("action"), action));
            }

            if (performedBy != null && !performedBy.isEmpty()) {
                predicates.add(cb.equal(root.get("performedBy"), performedBy));
            }

            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), start));
            }

            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}