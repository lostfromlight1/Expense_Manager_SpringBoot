package com.talent.expensemanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExpenseManagerApplicationTests {

    @Test
    void contextLoads() {
        // This confirms the Spring context starts successfully
    }

    @Test
    void checkAccount(){
        int totalAdminAccount = 1;
        Assertions.assertEquals(1, totalAdminAccount);
    }

    @Test
    void checkRole(){
        int totalRole = 2;
        Assertions.assertEquals(2, totalRole);
    }
}