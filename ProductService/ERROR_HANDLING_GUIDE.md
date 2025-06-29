# ProductService Error Handling Guide

## Common Internal Server Errors and Solutions

### 1. Database Connection Issues

**Symptoms:**
- `DataAccessException` or `SQLException`
- Service fails to start
- Database operation failures

**Causes:**
- MySQL server not running
- Wrong database credentials
- Database doesn't exist
- Network connectivity issues

**Solutions:**
```bash
# Check if MySQL is running
mysql -u root -p -e "SELECT 1;"

# Create database if it doesn't exist
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS insuranceproductdb;"

# Verify connection from application
curl http://localhost:8083/product/health
```

### 2. Auto-Increment Primary Key Issues

**Symptoms:**
- `Field 'product_id' doesn't have a default value`
- `DataAccessException` during insert operations
- Database constraint violations

**Causes:**
- Table not created with proper auto-increment primary key
- Incorrect table schema
- Hibernate DDL generation issues

**Solutions:**
```sql
-- Fix the table structure
DROP TABLE IF EXISTS PRODUCTS;

CREATE TABLE PRODUCTS (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    product_type VARCHAR(255) NOT NULL,
    coverage_type VARCHAR(255) NOT NULL,
    min_premium BIGINT NOT NULL,
    max_coverage BIGINT NOT NULL,
    description TEXT NOT NULL,
    is_active TINYINT(1) DEFAULT 1
);
```

**Application Configuration:**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # Temporarily use this to recreate tables
    properties:
      hibernate:
        id:
          new_generator_mappings: true
```

### 3. Boolean Field Mapping Issues

**Symptoms:**
- `HttpMessageNotReadableException`
- Boolean fields not properly serialized/deserialized
- Database boolean mapping errors

**Causes:**
- Lombok `@Data` annotation conflicts with boolean fields
- MySQL TINYINT(1) mapping issues
- JSON serialization problems

**Solutions:**
- Custom getter/setter methods are implemented
- Hibernate properties configured in application.yaml
- Use explicit boolean values in JSON requests

### 4. Validation Errors

**Symptoms:**
- `MethodArgumentNotValidException`
- `HttpMessageNotReadableException`
- 400 Bad Request responses

**Causes:**
- Missing required fields
- Invalid data types
- Negative values for premium/coverage

**Solutions:**
- Ensure all required fields are provided
- Use positive values for minPremium and maxCoverage
- Check JSON format and data types

### 5. OAuth2 Authentication Issues

**Symptoms:**
- 401 Unauthorized responses
- JWT token validation failures
- Auth0 connection issues

**Causes:**
- Missing or invalid JWT token
- Incorrect issuer URI
- Network issues reaching Auth0

**Solutions:**
```bash
# Test without authentication (if needed for debugging)
# Temporarily disable security in WebSecurityConfig

# Verify Auth0 configuration
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8083/product/health
```

### 6. Service Dependencies Issues

**Symptoms:**
- Service fails to start
- Connection timeouts
- Service discovery failures

**Causes:**
- Eureka Service Registry not running
- Config Server not available
- Network connectivity issues

**Solutions:**
```bash
# Check if Eureka is running
curl http://localhost:8761/eureka/apps

# Check if Config Server is running
curl http://localhost:9296/actuator/health

# Verify service registration
curl http://localhost:8761/eureka/apps/PRODUCT-SERVICE
```

## Testing and Debugging

### 1. Health Check Endpoint
```bash
curl http://localhost:8083/product/health
```

### 2. Test Product Creation
```bash
curl -X POST http://localhost:8083/product \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "productName": "Test Product",
    "productType": "Test",
    "coverageType": "Basic",
    "minPremium": 1000,
    "maxCoverage": 10000,
    "description": "Test description",
    "isActive": true
  }'
```

### 3. Check Application Logs
```bash
# Look for specific error patterns
grep -i "error\|exception\|failed" logs/application.log

# Check database connection logs
grep -i "database\|mysql\|connection" logs/application.log
```

### 4. Database Verification
```sql
-- Check table structure
DESCRIBE PRODUCTS;

-- Check boolean column mapping
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'PRODUCTS' AND COLUMN_NAME = 'IS_ACTIVE';

-- Test boolean operations
SELECT product_id, product_name, is_active,
       CASE WHEN is_active = 1 THEN 'TRUE' 
            WHEN is_active = 0 THEN 'FALSE' 
            ELSE 'NULL' END as is_active_text
FROM PRODUCTS;
```

## Configuration Checklist

### Application Properties
- [ ] Database URL: `jdbc:mysql://localhost:3306/insuranceproductdb`
- [ ] Database credentials: `root/root`
- [ ] JPA dialect: `MySQL8Dialect`
- [ ] Hibernate DDL: `create-drop` (temporarily) or `update`
- [ ] Boolean mapping properties configured
- [ ] Auto-increment ID generation enabled

### Security Configuration
- [ ] OAuth2 resource server enabled
- [ ] JWT authentication configured
- [ ] Auth0 issuer URI set correctly
- [ ] All endpoints require authentication

### Service Dependencies
- [ ] Eureka client enabled
- [ ] Config server import configured
- [ ] Service name: `PRODUCT-SERVICE`
- [ ] Port: `8083`

## Error Response Format

All errors now return a consistent format:
```json
{
  "errorMessage": "Description of the error",
  "errorCode": "ERROR_CODE"
}
```

### Common Error Codes
- `DATABASE_ERROR`: Database operation failures
- `VALIDATION_ERROR`: Input validation failures
- `INVALID_REQUEST_FORMAT`: JSON parsing errors
- `PRODUCT_NOT_FOUND`: Product not found
- `DUPLICATE_PRODUCT`: Product already exists
- `INTERNAL_SERVER_ERROR`: Unexpected errors

## Monitoring and Alerts

### Key Metrics to Monitor
- Database connection pool status
- JWT token validation success rate
- Request/response times
- Error rates by endpoint
- Service discovery status

### Log Patterns to Watch
- `DataAccessException`
- `HttpMessageNotReadableException`
- `ProductServiceCustomException`
- Authentication failures
- Database connection timeouts 