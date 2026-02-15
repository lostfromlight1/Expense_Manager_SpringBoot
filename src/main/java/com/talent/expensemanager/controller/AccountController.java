package com.talent.expensemanager.controller;

import com.talent.expensemanager.request.AccountRequest;
import com.talent.expensemanager.request.LoginRequest;
import com.talent.expensemanager.response.AccountResponse;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AccountResponse>> register(@RequestBody AccountRequest request) {
        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .success(true)
                .message("Account created successfully")
                .data(accountService.register(request))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AccountResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .success(true)
                .message("Login successful")
                .data(accountService.login(request.getEmail(), request.getPassword()))
                .build());
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<BaseResponse<AccountResponse>> getAccount(@PathVariable String id) {
        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .success(true)
                .message("Profile retrieved")
                .data(accountService.getById(id))
                .build());
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<BaseResponse<AccountResponse>> updateAccount(
            @PathVariable String id,
            @RequestBody AccountRequest request) {
        return ResponseEntity.ok(BaseResponse.<AccountResponse>builder()
                .success(true)
                .message("Account updated successfully")
                .data(accountService.updateAccount(id, request))
                .build());
    }

    @DeleteMapping("/terminate/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteAccount(@PathVariable String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .success(true)
                .message("Account and all associated data permanently deleted")
                .build());
    }
}