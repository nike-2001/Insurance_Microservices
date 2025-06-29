package com.nikhilspring.PolicyService.exception;

import com.nikhilspring.PolicyService.external.response.ErrorResponse;
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

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handlePolicyServiceException(CustomException exception, WebRequest request) {
        log.error("Policy Service Custom Exception: {}", exception.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorMessage(exception.getMessage())
                .errorCode(exception.getErrorCode())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(exception.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception, WebRequest request) {
        log.error("Generic Exception in Policy Service: {}", exception.getMessage(), exception);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorMessage("An unexpected error occurred: " + exception.getMessage())
                .errorCode("INTERNAL_SERVER_ERROR")
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception, WebRequest request) {
        log.error("Illegal Argument Exception: {}", exception.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorMessage(exception.getMessage())
                .errorCode("INVALID_INPUT")
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exception, WebRequest request) {
        log.error("Runtime Exception: {}", exception.getMessage(), exception);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorMessage("Service error: " + exception.getMessage())
                .errorCode("SERVICE_ERROR")
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
