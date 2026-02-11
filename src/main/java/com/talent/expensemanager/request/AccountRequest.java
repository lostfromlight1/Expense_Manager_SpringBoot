package com.talent.expensemanager.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountRequest {
    private String name;
    private LocalDate dateOfBirth;
    private String email;
    private String password;
    private boolean isActive;
}