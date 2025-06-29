# Claim Validation Guide

This document outlines the comprehensive validation system implemented for the Claim Service to ensure data integrity and prevent invalid claims.

## Validation Layers

### 1. Basic Validation (ClaimValidationUtil)
- **Policy ID**: Must be > 0
- **Customer ID**: Cannot be null/empty, must match format `^[A-Z0-9]+$`
- **Claim Amount**: Must be > 0
- **Claim Type**: Cannot be null/empty
- **Description**: Cannot be null/empty

### 2. Policy Validation (ClaimValidationService)
- **Policy Existence**: Policy must exist in PolicyService
- **Policy Status**: Must be "ACTIVE"
- **Policy Dates**: Must be within valid start/end date range

### 3. Customer Validation (ClaimValidationService)
- **Customer Format**: Must contain only uppercase letters and numbers
- **Customer-Policy Match**: Customer ID must match the policy's customer
- **Payment Validation**: Customer must have made payment for the policy

### 4. Payment Validation (ClaimValidationService)
- **Payment Existence**: Payment record must exist for the policy
- **Payment Status**: Payment must be "SUCCESS"

### 5. Duplicate Claim Validation
- **No Existing Claims**: Policy must not have an existing claim

## Test Scenarios

### Policy Validation Tests
1. **Non-existent Policy (ID: 99999)**
   - Expected: `POLICY_NOT_FOUND` error
   - Status: 404

2. **Invalid Policy ID (ID: 0)**
   - Expected: `INVALID_POLICY_ID` error
   - Status: 400

3. **Negative Policy ID (ID: -1)**
   - Expected: `INVALID_POLICY_ID` error
   - Status: 400

### Customer Validation Tests
4. **Null Customer ID**
   - Expected: `INVALID_CUSTOMER_ID` error
   - Status: 400

5. **Empty Customer ID**
   - Expected: `INVALID_CUSTOMER_ID` error
   - Status: 400

6. **Invalid Format - Lowercase (cust001)**
   - Expected: `INVALID_CUSTOMER_ID_FORMAT` error
   - Status: 400

7. **Invalid Format - Special Characters (CUST-001)**
   - Expected: `INVALID_CUSTOMER_ID_FORMAT` error
   - Status: 400

8. **Mismatched Customer ID (WRONGCUST)**
   - Expected: `CUSTOMER_MISMATCH` error
   - Status: 403

### Success Scenario
9. **Valid Policy and Customer (ID: 1, CUST001)**
   - Expected: Claim created successfully
   - Status: 200/201

## Error Codes

| Error Code | Description | HTTP Status |
|------------|-------------|-------------|
| `INVALID_POLICY_ID` | Policy ID is <= 0 | 400 |
| `POLICY_NOT_FOUND` | Policy doesn't exist | 404 |
| `POLICY_NOT_ACTIVE` | Policy is not active | 400 |
| `POLICY_NOT_STARTED` | Policy start date is in future | 400 |
| `POLICY_EXPIRED` | Policy end date has passed | 400 |
| `INVALID_CUSTOMER_ID` | Customer ID is null/empty | 400 |
| `INVALID_CUSTOMER_ID_FORMAT` | Customer ID format is invalid | 400 |
| `CUSTOMER_MISMATCH` | Customer doesn't match policy | 403 |
| `PAYMENT_NOT_FOUND` | No payment found for policy | 404 |
| `PAYMENT_NOT_SUCCESSFUL` | Payment status is not success | 400 |
| `CLAIM_ALREADY_EXISTS` | Claim already exists for policy | 409 |
| `CLAIM_AMOUNT_EXCEEDS_COVERAGE` | Claim amount > policy coverage | 400 |

## Running Tests

Use the test script to validate all scenarios:

```bash
chmod +x test_claim_validation.sh
./test_claim_validation.sh
```

## Implementation Details

### Feign Client Configuration
- Custom error decoder handles 404 responses properly
- JWT token forwarding for authenticated requests
- Proper exception handling for service communication

### Validation Flow
1. Basic validation (ClaimValidationUtil)
2. Policy existence and status validation
3. Customer format and policy match validation
4. Payment status validation
5. Duplicate claim check
6. Claim creation

### Logging
- Comprehensive logging at each validation step
- Error logging with context (policy ID, customer ID)
- Success logging for audit trail

## Security Considerations

- Customer ID format validation prevents injection attacks
- Policy-customer matching prevents unauthorized claims
- Payment validation ensures financial integrity
- JWT token validation for authenticated requests 