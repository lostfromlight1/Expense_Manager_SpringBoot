package com.talent.expensemanager.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.talent.expensemanager.response.BaseResponse;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final AuthenticationService authenticationService;
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    public AuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String apiId = request.getServletPath().replace("/api/v1/", "");

        if (requestUri.contains("/swagger-ui") || requestUri.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Authentication authentication = authenticationService.doAuthentication(request);

            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            LOGGER.error("Authentication failed for URI: {} - Error: {}", requestUri, e.getMessage());
            handleException(response, requestUri, apiId, e);
        }
    }

    private void handleException(HttpServletResponse response, String uri, String apiId, Exception e) throws IOException {
        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        BaseResponse<Void> errorResponse = BaseResponse.<Void>builder()
                .httpStatusCode(HttpServletResponse.SC_UNAUTHORIZED)
                .apiName(uri)
                .apiId(apiId)
                .message(e.getMessage())
                .build();

        try (PrintWriter writer = response.getWriter()) {
            writer.print(MAPPER.writeValueAsString(errorResponse));
            writer.flush();
        }
    }
}