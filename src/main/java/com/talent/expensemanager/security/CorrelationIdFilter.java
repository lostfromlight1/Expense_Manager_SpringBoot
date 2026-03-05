package com.talent.expensemanager.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setHeader("X-Trace-ID", traceId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }
}