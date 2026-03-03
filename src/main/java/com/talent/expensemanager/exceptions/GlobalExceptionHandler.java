package com.talent.expensemanager.exceptions;

import com.talent.expensemanager.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        BaseResponse<Map<String, String>> response = BaseResponse.<Map<String, String>>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("validationError")
                .apiId("validation-exception")
                .message("Input validation failed")
                .data(errors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccountException(AccountException ex) {
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("accountError")
                .apiId("account-exception")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WalletException.class)
    public ResponseEntity<BaseResponse<Void>> handleWalletException(WalletException ex) {
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("walletError")
                .apiId("wallet-exception")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<BaseResponse<Void>> handleTransactionException(TransactionException ex) {
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .apiName("transactionError")
                .apiId("transaction-exception")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGeneralException(Exception ex) {
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .apiName("systemError")
                .apiId("system-exception")
                .message("An unexpected error occurred: " + ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}