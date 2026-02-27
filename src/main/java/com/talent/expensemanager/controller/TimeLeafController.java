package com.talent.expensemanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TimeLeafController {

    @GetMapping("/test-ui")
    public String testPage() {
        return "test";
    }
}