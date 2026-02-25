package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.model.AuditLog;
import com.talent.expensemanager.repository.AuditLogRepository;
import com.talent.expensemanager.response.AuditResponse;
import com.talent.expensemanager.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditServiceImpl.class);
    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String action, String entityName, String entityId, String details, String performedBy) {
        LOGGER.info("AuditLog SYSTEM : Recording action {} on {} is started.", action, entityName);

        AuditLog logEntry = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(logEntry);

        LOGGER.info("\nAuditLog SYSTEM : {} SUCCESS", action);
    }

    @Override
    public List<AuditResponse> getLogsByUser(String accountId) {
        LOGGER.info("getLogsByUser SYSTEM : Fetching logs for user {}", accountId);

        return auditLogRepository.findByPerformedByOrderByTimestampDesc(accountId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditResponse> getAllLogs() {
        LOGGER.info("getAllLogs SYSTEM : Fetching all system logs");

        return auditLogRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditResponse mapToResponse(AuditLog log) {
        return AuditResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .performedBy(log.getPerformedBy())
                .timestamp(log.getTimestamp())
                .build();
    }
}