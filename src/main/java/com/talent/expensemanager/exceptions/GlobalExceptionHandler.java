package com.talent.expensemanager.exceptions;

import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final AuditService auditService;

    // --- HELPER: GET CURRENT USER FOR AUDIT ---
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            // Assuming the principal is the accountId/String. 
            // If it's a UserDetails object, you'd use auth.getName()
            return auth.getPrincipal().toString();
        }
        return "GUEST_USER";
    }

    // --- RESOURCE NOT FOUND (404) ---
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        auditService.logError("NOT_FOUND", ex.getMessage(), getCurrentUser(), "404");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.NOT_FOUND.value())
                .apiName("resourceNotFound")
                .apiId("id-not-found")
                .message(ex.getMessage())
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    // --- DOMAIN EXCEPTIONS (400) ---
    @ExceptionHandler({AccountException.class, WalletException.class, TransactionException.class})
    public ResponseEntity<BaseResponse<Void>> handleDomainExceptions(RuntimeException ex) {
        String category = ex.getClass().getSimpleName().replace("Exception", "").toUpperCase();
        auditService.logError(category + "_ERROR", ex.getMessage(), getCurrentUser(), "400");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("domainError")
                .apiId(category.toLowerCase() + "-exception")
                .message(ex.getMessage())
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    // --- VALIDATION & JSON ERRORS (400) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        auditService.logError("VALIDATION_ERROR", "Field validation failed: " + errors, getCurrentUser(), "400");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.<Map<String, String>>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("validationError")
                .apiId("validation-exception")
                .message("Input validation failed")
                .systemDateTime(LocalDateTime.now())
                .data(errors)
                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleJsonErrors(HttpMessageNotReadableException ex) {
        auditService.logError("MALFORMED_JSON", "Invalid request body format", getCurrentUser(), "400");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("invalidInput")
                .apiId("json-parse-error")
                .message("Invalid request body. Check your Enum values or JSON format.")
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    // --- SECURITY (403 & 401) ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        auditService.logError("SECURITY_ACCESS_DENIED", "Unauthorized attempt to resource", getCurrentUser(), "403");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.FORBIDDEN.value())
                .apiName("accessDenied")
                .apiId("security-forbidden")
                .message("You do not have permission to access this resource.")
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuth(AuthenticationException ex) {
        auditService.logError("AUTH_FAILURE", ex.getMessage(), "GUEST_USER", "401");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                .apiName("authError")
                .apiId("security-unauthorized")
                .message(ex.getMessage())
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    // --- CATCH-ALL (500) ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGeneralException(Exception ex) {
        LOGGER.error("CRITICAL SYSTEM ERROR: ", ex);
        auditService.logError("INTERNAL_SERVER_ERROR", ex.getMessage(), "SYSTEM", "500");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .apiName("systemError")
                .apiId("system-exception")
                .message("An unexpected error occurred. Please contact support.")
                .systemDateTime(LocalDateTime.now())
                .build());
    }
}