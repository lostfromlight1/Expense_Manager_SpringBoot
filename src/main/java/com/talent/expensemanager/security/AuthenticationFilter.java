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

    private final AuthenticationService authenticationService;
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    public AuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Added refresh-token to the list of public endpoints
        return path.equals("/api/v1/accounts/register") ||
                path.equals("/api/v1/accounts/login") ||
                path.equals("/api/v1/accounts/refresh-token");
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // Extract a cleaner apiId for your BaseResponse
        String apiId = request.getServletPath().replace("/api/v1/", "");

        try {
            // This now handles JWT extraction and API Key validation
            Authentication authentication = authenticationService.doAuthentication(request);

            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Log the error here if you have a LOGGER
            handleException(response, requestUri, apiId, e);
        } finally {
            // Optional: Explicitly clear context after request finishes in stateless apps
            // SecurityContextHolder.clearContext();
        }
    }

    private void handleException(HttpServletResponse response, String uri, String apiId, Exception e) throws IOException {
        SecurityContextHolder.clearContext();

        // Use 401 for Auth issues
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        BaseResponse<Void> errorResponse = BaseResponse.<Void>builder()
                .httpStatusCode(HttpServletResponse.SC_UNAUTHORIZED)
                .apiName(uri)
                .apiId(apiId)
                .message(e.getMessage()) // This will show "Expired JWT" or "Invalid API Key"
                .build();

        try (PrintWriter writer = response.getWriter()) {
            writer.print(MAPPER.writeValueAsString(errorResponse));
            writer.flush();
        }
    }
    public String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) return null;
        return MAPPER.writeValueAsString(object);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
