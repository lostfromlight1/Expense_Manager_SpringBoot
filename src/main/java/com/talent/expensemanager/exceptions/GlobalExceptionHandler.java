package com.talent.expensemanager.exceptions;

import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final AuditService auditService;

    private String getTraceId() {
        return MDC.get("traceId");
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getPrincipal().toString();
        }
        return "GUEST_USER";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleStaticResourceNotFound(NoResourceFoundException ex) {
        String tid = getTraceId();
        auditService.logError("ROUTE_NOT_FOUND", "[404] " + ex.getMessage(), "SYSTEM", tid);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.NOT_FOUND.value())
                .apiName("routeNotFound")
                .apiId("static-resource-404")
                .traceId(tid)
                .message("The requested resource does not exist.")
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        String tid = getTraceId();
        auditService.logError("NOT_FOUND", "[404] " + ex.getMessage(), getCurrentUser(), tid);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.NOT_FOUND.value())
                .apiName("resourceNotFound")
                .apiId("id-not-found")
                .traceId(tid)
                .message(ex.getMessage())
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler({AccountException.class, WalletException.class, TransactionException.class})
    public ResponseEntity<BaseResponse<Void>> handleDomainExceptions(RuntimeException ex) {
        String tid = getTraceId();
        String category = ex.getClass().getSimpleName().replace("Exception", "").toUpperCase();
        auditService.logError(category + "_ERROR", "[400] " + ex.getMessage(), getCurrentUser(), tid);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("domainError")
                .apiId(category.toLowerCase() + "-exception")
                .traceId(tid)
                .message(ex.getMessage())
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        String tid = getTraceId();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        auditService.logError("VALIDATION_ERROR", "[400] Field validation failed: " + errors, getCurrentUser(), tid);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.<Map<String, String>>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("validationError")
                .apiId("validation-exception")
                .traceId(tid)
                .message("Input validation failed")
                .systemDateTime(LocalDateTime.now())
                .data(errors)
                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleJsonErrors(HttpMessageNotReadableException ex) {
        String tid = getTraceId();
        auditService.logError("MALFORMED_JSON", "[400] Invalid request body format", getCurrentUser(), tid);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("invalidInput")
                .apiId("json-parse-error")
                .traceId(tid)
                .message("Invalid request body. Check your Enum values or JSON format.")
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    // --- SECURITY (403) ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        String tid = getTraceId();
        auditService.logError("SECURITY_ACCESS_DENIED", "[403] Unauthorized attempt", getCurrentUser(), tid);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.FORBIDDEN.value())
                .apiName("accessDenied")
                .apiId("security-forbidden")
                .traceId(tid)
                .message("You do not have permission to access this resource.")
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuth(AuthenticationException ex) {
        String tid = getTraceId();
        auditService.logError("AUTH_FAILURE", "[401] " + ex.getMessage(), "GUEST_USER", tid);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                .apiName("authError")
                .apiId("security-unauthorized")
                .traceId(tid)
                .message(ex.getMessage())
                .systemDateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGeneralException(Exception ex) {
        String tid = getTraceId();
        LOGGER.error("CRITICAL SYSTEM ERROR [TraceID: {}]: ", tid, ex);
        auditService.logError("INTERNAL_SERVER_ERROR", "[500] " + ex.getMessage(), "SYSTEM", tid);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .apiName("systemError")
                .apiId("system-exception")
                .traceId(tid)
                .message("An unexpected error occurred. Please contact support. Error Reference: " + tid)
                .systemDateTime(LocalDateTime.now())
                .build());
    }
}