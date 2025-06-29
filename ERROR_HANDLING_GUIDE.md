# 🛡️ Error Handling & Fallback Mechanisms Guide

## Overview
This document outlines the comprehensive error handling and fallback mechanisms implemented across all microservices in the Insurance Platform.

## 🏗️ Architecture

### Error Handling Structure
```
├── Custom Exceptions
│   ├── PaymentServiceCustomException
│   ├── ClaimServiceCustomException
│   ├── ProductServiceCustomException
│   └── CustomException (Policy Service)
├── Validation Utilities
│   ├── PaymentValidationUtil
│   ├── ClaimValidationUtil
│   ├── ProductValidationUtil
│   └── PolicyValidationUtil
├── Exception Handlers
│   ├── RestResponseEntityExceptionHandler
│   └── Global Exception Handling
└── Fallback Mechanisms
    ├── Circuit Breakers
    ├── Fallback Controllers
    └── Service Degradation
```

## 🔧 Custom Exception Classes

### 1. PaymentServiceCustomException
- **Location**: `PaymentService/src/main/java/com/nikhilspring/PaymentService/exception/`
- **Features**:
  - Error code and status tracking
  - Customizable error messages
  - HTTP status code mapping

### 2. ClaimServiceCustomException
- **Location**: `ClaimService/src/main/java/com/nikhilspring/ClaimService/exception/`
- **Features**:
  - Comprehensive claim validation errors
  - Policy and claim ID validation
  - Duplicate claim prevention

### 3. ProductServiceCustomException
- **Location**: `ProductService/src/main/java/com/nikhilspring/ProductService/exception/`
- **Features**:
  - Product validation errors
  - CRUD operation error handling
  - Business rule validation

### 4. CustomException (Policy Service)
- **Location**: `PolicyService/src/main/java/com/nikhilspring/PolicyService/exception/`
- **Features**:
  - Policy-specific error handling
  - External service integration errors
  - Complex business logic validation

## 📋 Validation Utilities

### PaymentValidationUtil
```java
// Validates payment requests
PaymentValidationUtil.validatePaymentRequest(paymentRequest);

// Validates policy IDs
PaymentValidationUtil.validatePolicyId(policyId);

// Validates customer IDs
PaymentValidationUtil.validateCustomerId(customerId);
```

### ClaimValidationUtil
```java
// Validates claim requests
ClaimValidationUtil.validateClaimRequest(claimRequest);

// Validates claim IDs
ClaimValidationUtil.validateClaimId(claimId);

// Validates policy IDs
ClaimValidationUtil.validatePolicyId(policyId);
```

### ProductValidationUtil
```java
// Validates product requests
ProductValidationUtil.validateProductRequest(productRequest);

// Validates product IDs
ProductValidationUtil.validateProductId(productId);

// Validates product existence
ProductValidationUtil.validateProductExists(exists, productId);
```

### PolicyValidationUtil
```java
// Validates policy requests
PolicyValidationUtil.validatePolicyRequest(policyRequest);

// Validates policy IDs
PolicyValidationUtil.validatePolicyId(policyId);

// Validates policy existence
PolicyValidationUtil.validatePolicyExists(exists, policyId);
```

## 🚨 Error Response Format

All services return standardized error responses:

```json
{
    "errorMessage": "Descriptive error message",
    "errorCode": "UNIQUE_ERROR_CODE",
    "timestamp": "2024-01-01T12:00:00",
    "path": "/api/endpoint"
}
```

## 🔄 Fallback Mechanisms

### 1. Circuit Breakers
- **Implementation**: Resilience4j
- **Configuration**: Applied to external service calls
- **Fallback Methods**: Custom fallback responses

### 2. Cloud Gateway Fallbacks
- **Location**: `CloudGateway/src/main/java/com/nikhilspring/CloudGateway/controller/FallbackController`
- **Services Covered**:
  - Policy Service
  - Payment Service
  - Product Service
  - Claim Service

### 3. Service Degradation
- **Graceful Degradation**: Services continue with limited functionality
- **Partial Responses**: Return available data when possible
- **User Notifications**: Clear error messages to users

## 🧪 Testing Error Scenarios

### 1. Invalid Data Testing

#### Payment Service
```bash
# Test invalid policy ID
POST http://localhost:8081/payment
{
    "policyId": -1,
    "amount": 100,
    "paymentMode": "CREDIT_CARD"
}
# Expected: 400 Bad Request with INVALID_POLICY_ID error

# Test invalid amount
POST http://localhost:8081/payment
{
    "policyId": 1,
    "amount": 0,
    "paymentMode": "CREDIT_CARD"
}
# Expected: 400 Bad Request with INVALID_AMOUNT error
```

#### Product Service
```bash
# Test invalid product ID
GET http://localhost:8083/product/999999
# Expected: 404 Not Found with PRODUCT_NOT_FOUND error

# Test invalid product request
POST http://localhost:8083/product
{
    "productName": "",
    "minPremium": -100
}
# Expected: 400 Bad Request with validation errors
```

#### Claim Service
```bash
# Test invalid claim amount
POST http://localhost:8084/claim
{
    "policyId": 1,
    "claimAmount": -5000,
    "claimType": "AUTO_ACCIDENT"
}
# Expected: 400 Bad Request with INVALID_CLAIM_AMOUNT error
```

#### Policy Service
```bash
# Test invalid policy request
POST http://localhost:8082/policy/issue
{
    "customerId": 0,
    "productId": 999999,
    "premiumAmount": -100
}
# Expected: 400 Bad Request with validation errors
```

### 2. Service Unavailability Testing

#### Test Circuit Breaker
```bash
# Stop Product Service and test Policy Service
POST http://localhost:8082/policy/issue
# Expected: Circuit breaker fallback response
```

#### Test Gateway Fallback
```bash
# Stop Payment Service and test via Gateway
POST http://localhost:9090/payment
# Expected: Gateway fallback response
```

### 3. Duplicate Resource Testing

#### Payment Service
```bash
# Create payment for policy 1
POST http://localhost:8081/payment
{
    "policyId": 1,
    "amount": 100
}

# Try to create another payment for same policy
POST http://localhost:8081/payment
{
    "policyId": 1,
    "amount": 100
}
# Expected: 409 Conflict with PAYMENT_ALREADY_EXISTS error
```

#### Claim Service
```bash
# File claim for policy 1
POST http://localhost:8084/claim
{
    "policyId": 1,
    "claimAmount": 5000
}

# Try to file another claim for same policy
POST http://localhost:8084/claim
{
    "policyId": 1,
    "claimAmount": 3000
}
# Expected: 409 Conflict with CLAIM_ALREADY_EXISTS error
```

## 📊 Error Codes Reference

### Payment Service Error Codes
- `INVALID_REQUEST` - Request is null or invalid
- `INVALID_POLICY_ID` - Policy ID is invalid
- `INVALID_AMOUNT` - Payment amount is invalid
- `INVALID_PAYMENT_MODE` - Payment mode is null
- `INVALID_PAYMENT_TYPE` - Payment type is invalid
- `INVALID_CUSTOMER_ID` - Customer ID is invalid
- `INVALID_REFERENCE_NUMBER` - Reference number is invalid
- `PAYMENT_ALREADY_EXISTS` - Payment already exists for policy
- `PAYMENT_NOT_FOUND` - Payment not found
- `PAYMENT_PROCESSING_FAILED` - Payment processing failed
- `INVALID_POLICY_ID_FORMAT` - Policy ID format is invalid

### Product Service Error Codes
- `INVALID_REQUEST` - Request is null or invalid
- `INVALID_PRODUCT_NAME` - Product name is invalid
- `INVALID_PRODUCT_TYPE` - Product type is invalid
- `INVALID_COVERAGE_TYPE` - Coverage type is invalid
- `INVALID_MIN_PREMIUM` - Minimum premium is invalid
- `INVALID_MAX_COVERAGE` - Maximum coverage is invalid
- `INVALID_DESCRIPTION` - Description is invalid
- `INVALID_PRODUCT_ID` - Product ID is invalid
- `PRODUCT_NOT_FOUND` - Product not found
- `NOT_FOUND` - Resource not found

### Claim Service Error Codes
- `INVALID_REQUEST` - Request is null or invalid
- `INVALID_POLICY_ID` - Policy ID is invalid
- `INVALID_CLAIM_AMOUNT` - Claim amount is invalid
- `INVALID_CUSTOMER_ID` - Customer ID is invalid
- `INVALID_CLAIM_TYPE` - Claim type is invalid
- `INVALID_DESCRIPTION` - Description is invalid
- `INVALID_CLAIM_ID` - Claim ID is invalid
- `CLAIM_ALREADY_EXISTS` - Claim already exists for policy
- `CLAIM_NOT_FOUND` - Claim not found
- `CLAIM_FILING_FAILED` - Claim filing failed

### Policy Service Error Codes
- `INVALID_REQUEST` - Request is null or invalid
- `INVALID_CUSTOMER_ID` - Customer ID is invalid
- `INVALID_PRODUCT_ID` - Product ID is invalid
- `INVALID_PREMIUM_AMOUNT` - Premium amount is invalid
- `INVALID_COVERAGE_AMOUNT` - Coverage amount is invalid
- `INVALID_START_DATE` - Start date is invalid
- `INVALID_END_DATE` - End date is invalid
- `INVALID_DATE_RANGE` - Date range is invalid
- `INVALID_PAYMENT_MODE` - Payment mode is invalid
- `INVALID_POLICY_ID` - Policy ID is invalid
- `POLICY_NOT_FOUND` - Policy not found
- `NOT_FOUND` - Resource not found
- `UNAVAILABLE` - Service unavailable

## 🔧 Configuration

### Circuit Breaker Configuration
```yaml
resilience4j:
  circuitbreaker:
    instances:
      external:
        event-consumer-buffer-size: 10
        failure-rate-threshold: 50
        minimum-number-of-calls: 5
        automatic-transition-from-open-to-half-open-enabled: true
        wait-duration-in-open-state: 5s
        permitted-number-of-calls-in-half-open-state: 3
        sliding-window-size: 10
        sliding-window-type: COUNT_BASED
```

### Rate Limiting Configuration
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 1
            redis-rate-limiter.burstCapacity: 1
```

## 📈 Monitoring & Logging

### Error Logging
- All exceptions are logged with appropriate log levels
- Error codes and messages are standardized
- Timestamps and request paths are included

### Health Checks
- Each service includes health check endpoints
- Circuit breaker status monitoring
- Service availability tracking

## 🚀 Best Practices

1. **Always validate input data** before processing
2. **Use specific error codes** for different error types
3. **Provide meaningful error messages** to users
4. **Implement graceful degradation** when services are unavailable
5. **Log all errors** with appropriate context
6. **Test error scenarios** thoroughly
7. **Monitor error rates** and patterns
8. **Implement retry mechanisms** for transient failures

## 🔍 Troubleshooting

### Common Issues
1. **Validation errors**: Check input data format and requirements
2. **Service unavailable**: Check if dependent services are running
3. **Circuit breaker open**: Wait for circuit breaker to close or check service health
4. **Rate limiting**: Reduce request frequency or increase limits

### Debug Steps
1. Check service logs for detailed error messages
2. Verify service connectivity and health
3. Test individual service endpoints
4. Check circuit breaker status
5. Monitor resource usage and performance 