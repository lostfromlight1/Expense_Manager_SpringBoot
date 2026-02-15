package com.talent.expensemanager.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String accountId;
    private String name;
    private String email;
    private String dateOfBirth;
    private Boolean active;
}