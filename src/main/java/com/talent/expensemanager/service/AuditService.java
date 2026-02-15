package com.talent.expensemanager.service;

import com.talent.expensemanager.response.AuditResponse;
import java.util.List;

public interface AuditService {
    // For internal logging
    void log(String action, String entityName, String entityId, String details);

    // For frontend display
    List<AuditResponse> getAllLogs();
}