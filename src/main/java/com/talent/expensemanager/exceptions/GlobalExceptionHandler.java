package com.talent.expensemanager.exceptions;

import com.talent.expensemanager.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @SuppressWarnings("unused")
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

    // Catch-all for unexpected errors
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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
}