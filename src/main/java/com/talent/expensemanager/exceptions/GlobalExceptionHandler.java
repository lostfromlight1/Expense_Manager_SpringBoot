package com.talent.expensemanager.exceptions;

import com.talent.expensemanager.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccountException(AccountException ex) {
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("ACCOUNT_ERROR")
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Catch-all for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGeneralException(Exception ex) {
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .success(false)
                .message("An unexpected error occurred: " + ex.getMessage())
                .errorCode("SYSTEM_ERROR")
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(WalletException.class)
    public ResponseEntity<BaseResponse<Void>> handleWalletException(WalletException ex) {
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("WALLET_ERROR")
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<BaseResponse<Void>> handleTransactionException(TransactionException ex) {
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("TRANSACTION_ERROR")
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}