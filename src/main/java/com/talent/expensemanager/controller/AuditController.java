package com.talent.expensemanager.controller;

import com.talent.expensemanager.response.AuditResponse;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<AuditResponse>>> getAllLogs() {
        return ResponseEntity.ok(BaseResponse.<List<AuditResponse>>builder()
                .success(true)
                .message("Activity logs retrieved successfully")
                .data(auditService.getAllLogs())
                .build());
    }
}