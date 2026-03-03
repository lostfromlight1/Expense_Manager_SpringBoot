package com.talent.expensemanager.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ApiKeyConfiguration {
    @Value("${jwt.api-key}")
    private String apikey;

    @Value("${jwt.secret-token}")
    private String secretToken;

}
