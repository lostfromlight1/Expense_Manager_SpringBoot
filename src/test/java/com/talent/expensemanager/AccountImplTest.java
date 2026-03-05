package com.talent.expensemanager;

import com.talent.expensemanager.request.AccountRequest;
import com.talent.expensemanager.request.LoginRequest;
import com.talent.expensemanager.response.AccountResponse;
import com.talent.expensemanager.response.BaseResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountImplTest {

    @LocalServerPort
    private int port;

    @Value("${jwt.api-key}")
    private String apiKey;

    @Test
    void testAccountLifecycle() {
        RestClient restClient = RestClient.create("http://localhost:" + port);

        // 1. LOGIN (POST)
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@gmail.com");
        loginRequest.setPassword("123456");

        ResponseEntity<BaseResponse<AccountResponse>> loginResponse = restClient.post()
                .uri("/api/v1/accounts/login")
                .header("X-expense-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        Assertions.assertNotNull(loginResponse.getBody());
        AccountResponse loginData = loginResponse.getBody().getData();
        String token = loginData.getToken();
        String accountId = loginData.getAccountId();

        System.out.println("--- LOGIN SUCCESSFUL ---");
        System.out.println("Token: " + token);

        // 2. GET ACCOUNT INFO (GET) - Fixed URI to include /profile
        ResponseEntity<BaseResponse<AccountResponse>> getResponse = restClient.get()
                .uri("/api/v1/accounts/profile/{id}", accountId)
                .header("X-expense-api-key", apiKey)
                .header("token", token)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        Assertions.assertEquals(200, getResponse.getStatusCode().value());
        Assertions.assertNotNull(getResponse.getBody().getData());

        System.out.println("--- GET ACCOUNT INFO ---");
        System.out.println("Current Name: " + getResponse.getBody().getData().getName());
        System.out.println("Trace ID: " + getResponse.getBody().getTraceId());

        // 3. UPDATE ACCOUNT (PUT)
        AccountRequest updateRequest = new AccountRequest();
        updateRequest.setName("Updated AcePlus Admin");
        updateRequest.setEmail("admin@gmail.com");
        updateRequest.setDateOfBirth(LocalDate.of(1995, 5, 20));

        ResponseEntity<BaseResponse<AccountResponse>> updateResponse = restClient.put()
                .uri("/api/v1/accounts/{id}", accountId)
                .header("X-expense-api-key", apiKey)
                .header("token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        System.out.println("--- UPDATE ACCOUNT SUCCESS ---");
        Assertions.assertNotNull(updateResponse.getBody());
        Assertions.assertEquals("Updated AcePlus Admin", updateResponse.getBody().getData().getName());
        System.out.println("Message: " + updateResponse.getBody().getMessage());
        System.out.println("New Name: " + updateResponse.getBody().getData().getName());
        System.out.println("---------------------\n");
    }
}