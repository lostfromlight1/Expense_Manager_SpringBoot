package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.model.AuditLog;
import com.talent.expensemanager.repository.AuditLogRepository;
import com.talent.expensemanager.response.AuditResponse;
import com.talent.expensemanager.service.AuditService;
import com.talent.expensemanager.utils.AuditSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String action, String entityName, String entityId, String details, String performedBy) {
        AuditLog logEntry = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(logEntry);
    }

    @Override
    public void logError(String action, String details, String performedBy, String ignoredCode) {
        String traceId = org.slf4j.MDC.get("traceId");

        AuditLog logEntry = AuditLog.builder()
                .action("ERROR_" + action)
                .entityName("SYSTEM_EXCEPTION")
                .entityId(traceId != null ? traceId : "SYSTEM")
                .details(details)
                .performedBy(performedBy != null ? performedBy : "SYSTEM")
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(logEntry);
    }

    @Override
    public Page<AuditResponse> getAllLogs(String action, String performedBy, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        log.info("Searching audit logs - Action: {}, User: {}", action, performedBy);

        Specification<AuditLog> spec = AuditSpecifications.buildSearchQuery(action, performedBy, start, end);
        return auditLogRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    public List<AuditResponse> getLogsByUser(String accountId) {
        return auditLogRepository.findByPerformedByOrderByTimestampDesc(accountId)
                .stream()
                .filter(audit -> !audit.getAction().startsWith("ERROR_")) // Hide system errors from users
                .map(this::mapToResponse)
                .toList();
    }

    private AuditResponse mapToResponse(AuditLog auditLog) {
        return AuditResponse.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .entityName(auditLog.getEntityName())
                .entityId(auditLog.getEntityId())
                .details(auditLog.getDetails())
                .performedBy(auditLog.getPerformedBy())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}