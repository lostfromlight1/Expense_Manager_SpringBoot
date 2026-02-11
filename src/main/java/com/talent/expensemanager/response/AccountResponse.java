package com.talent.expensemanager.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String accountId;
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private Boolean isActive;

    public AccountResponse(String accountId, String name, String email) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
    }

    public AccountResponse(String accountId, String name, String email, LocalDate dateOfBirth) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }
}