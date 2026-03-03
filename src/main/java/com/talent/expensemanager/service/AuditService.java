package com.talent.expensemanager.service;

import com.talent.expensemanager.response.AuditResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {
    void log(String action, String entityName, String entityId, String details, String performedBy);

    Page<AuditResponse> getAllLogs(String action, String performedBy, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<AuditResponse> getLogsByUser(String accountId);

    void logError(String action, String details, String performedBy, String errorCode);
}