package com.talent.expensemanager.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditResponse {
    private Long id;
    private String action;
    private String entityName;
    private String entityId;
    private String details;
    private String performedBy;
    private LocalDateTime timestamp;
}