package com.talent.expensemanager.controller;

import com.talent.expensemanager.response.AuditResponse;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuditController {

    private final AuditService auditService;
    @GetMapping("/user/{accountId}")
    public ResponseEntity<BaseResponse<List<AuditResponse>>> getUserLogs(@PathVariable String accountId) {
        return ResponseEntity.ok(BaseResponse.<List<AuditResponse>>builder()
                .success(true)
                .message("User activity logs retrieved")
                .data(auditService.getLogsByUser(accountId))
                .build());
    }
    @GetMapping
    public ResponseEntity<BaseResponse<List<AuditResponse>>> getAllLogs() {
        return ResponseEntity.ok(BaseResponse.<List<AuditResponse>>builder()
                .success(true)
                .message("All activity logs retrieved")
                .data(auditService.getAllLogs())
                .build());
    }
}