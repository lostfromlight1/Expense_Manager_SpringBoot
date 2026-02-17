package com.talent.expensemanager.service;

import com.talent.expensemanager.response.AuditResponse;

import java.util.List;

public interface AuditService {
    void log(String action, String entityName, String entityId, String details, String performedBy);

    List<AuditResponse> getAllLogs();

    List<AuditResponse> getLogsByUser(String accountId); // Add this
}