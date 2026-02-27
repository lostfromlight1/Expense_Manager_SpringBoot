package com.talent.expensemanager.controller;

import com.talent.expensemanager.response.AuditResponse;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/user/{accountId}")
    @PreAuthorize("@permissionSecurity.hasAccountAccess(#accountId)")
    public ResponseEntity<BaseResponse<List<AuditResponse>>> getUserLogs(@PathVariable String accountId) {
        return ResponseEntity.ok(BaseResponse.<List<AuditResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getUserAuditLogs")
                .apiId("audit-user-get")
                .message("User activity logs retrieved")
                .data(auditService.getLogsByUser(accountId))
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<AuditResponse>>> getAllLogs() {
        return ResponseEntity.ok(BaseResponse.<List<AuditResponse>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("getAllAuditLogs")
                .apiId("audit-all-get")
                .message("All system activity logs retrieved")
                .data(auditService.getAllLogs())
                .build());
    }
}