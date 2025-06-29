package com.nikhilspring.ClaimService.exception;

import com.nikhilspring.ClaimService.model.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@Log4j2
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ClaimServiceCustomException.class)
    public ResponseEntity<ErrorResponse> handleClaimServiceException(ClaimServiceCustomException exception, WebRequest request) {
        log.error("Claim Service Exception: {}", exception.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorMessage(exception.getMessage())
                .errorCode(exception.getErrorCode())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(exception.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception, WebRequest request) {
        log.error("Generic Exception: {}", exception.getMessage(), exception);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorMessage("An unexpected error occurred")
                .errorCode("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception, WebRequest request) {
        log.error("Illegal Argument Exception: {}", exception.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorMessage(exception.getMessage())
                .errorCode("INVALID_INPUT")
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
} 