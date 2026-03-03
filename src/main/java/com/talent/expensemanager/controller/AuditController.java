package com.talent.expensemanager.controller;

import com.talent.expensemanager.response.AuditResponse;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Page<AuditResponse>>> getAllLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditResponse> pagedLogs = auditService.getAllLogs(action, performedBy, start, end, pageable);

        return ResponseEntity.ok(BaseResponse.<Page<AuditResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getAllAuditLogs")
                .message("Search completed successfully")
                .data(pagedLogs)
                .build());
    }

    @GetMapping("/user/{accountId}")
    @PreAuthorize("@permissionSecurity.hasAccountAccess(#accountId)")
    public ResponseEntity<BaseResponse<List<AuditResponse>>> getUserLogs(@PathVariable String accountId) {
        return ResponseEntity.ok(BaseResponse.<List<AuditResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .data(auditService.getLogsByUser(accountId))
                .build());
    }
}