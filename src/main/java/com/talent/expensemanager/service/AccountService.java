package com.talent.expensemanager.service;

import com.talent.expensemanager.request.AccountRequest;
import com.talent.expensemanager.response.AccountResponse;
import java.util.List;

public interface AccountService {
    AccountResponse register(AccountRequest request);

    AccountResponse login(String email, String password);

    AccountResponse getById(String id);

    AccountResponse updateAccount(String id, AccountRequest request);

    void changePassword(String id, String oldPassword, String newPassword);

    void deleteAccount(String id);

    List<AccountResponse> getAllAccounts();
}