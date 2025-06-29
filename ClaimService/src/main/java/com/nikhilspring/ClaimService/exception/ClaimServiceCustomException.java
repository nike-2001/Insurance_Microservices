package com.nikhilspring.ClaimService.exception;

import lombok.Data;

@Data
public class ClaimServiceCustomException extends RuntimeException {

    private final String errorCode;
    private final int status;

    public ClaimServiceCustomException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ClaimServiceCustomException(String message, String errorCode) {
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