package com.nikhilspring.PaymentService.exception;

import lombok.Data;

@Data
public class PaymentServiceCustomException extends RuntimeException {

    private final String errorCode;
    private final int status;

    public PaymentServiceCustomException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public PaymentServiceCustomException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.status = 400;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatus() {
        return status;
    }
} 