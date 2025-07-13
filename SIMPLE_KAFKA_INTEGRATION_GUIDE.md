# Simple Kafka Integration Guide

## Overview
This project implements a simple Kafka integration with Avro schemas for payment event processing. The PaymentService acts as a producer, while PolicyService and ClaimService act as consumers.

## Architecture

```
PaymentService (Producer) 
       ↓
   payment-events topic
       ↓
PolicyService & ClaimService (Consumers)
```

## How It Works

### 1. Payment Events Flow
- When a payment is processed in PaymentService, it publishes a PaymentEvent to Kafka
- PolicyService listens for premium payment completions to activate policies
- ClaimService listens for claim payment confirmations to update claim status

### 2. Event Types
- **PREMIUM**: Policy premium payments → activates policies
- **CLAIM_PAYMENT**: Claim payments → updates claim status

## Usage Examples

### In PaymentService - Publishing Events

```java
@Autowired
private PaymentEventProducer paymentEventProducer;

// When a policy premium is paid
paymentEventProducer.publishPolicyPaymentCompleted(paymentId, policyId, amount);

// When a claim payment is made
paymentEventProducer.publishClaimPaymentCompleted(paymentId, claimId, amount);
```

### In PolicyService - Processing Events
The `PaymentEventConsumer` automatically processes payment events and logs policy activations.

### In ClaimService - Processing Events  
The `PaymentEventConsumer` automatically processes claim payment events and logs status updates.

## Quick Start

### 1. Start Kafka Infrastructure
```bash
docker-compose up -d zookeeper kafka schema-registry
```

### 2. Build Services
```bash
# Generate Avro classes
cd PaymentService && mvn clean generate-sources
cd ../PolicyService && mvn clean generate-sources  
cd ../ClaimService && mvn clean generate-sources
```

### 3. Start Services
```bash
docker-compose up -d
```

### 4. Test the Integration
```bash
# Make a payment via PaymentService API
curl -X POST http://localhost:8081/payments \
  -H "Content-Type: application/json" \
  -d '{
    "policyId": 1,
    "amount": 1000.0,
    "paymentMode": "CREDIT_CARD"
  }'
```

### 5. Check Logs
```bash
# Check PolicyService logs for activation
docker logs policyservice

# Check ClaimService logs (if claim payment)
docker logs claimservice
```

## Configuration

### Environment Variables
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker address (default: localhost:9092)
- `SCHEMA_REGISTRY_URL`: Schema registry URL (default: http://localhost:8081)

### Topic Configuration
- **Topic Name**: `payment-events`
- **Partitions**: 1 (for simplicity)
- **Auto-created**: Yes

## Monitoring

### Kafka Topics
```bash
# List topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# View messages
docker exec kafka kafka-console-consumer --topic payment-events --bootstrap-server localhost:9092 --from-beginning
```

### Schema Registry
```bash
# Check registered schemas
curl http://localhost:8081/subjects
```

## Integration Points

### PaymentService
- **File**: `PaymentEventProducer.java`
- **Purpose**: Publishes payment completion events
- **Usage**: Call `publishPolicyPaymentCompleted()` or `publishClaimPaymentCompleted()`

### PolicyService  
- **File**: `PaymentEventConsumer.java`
- **Purpose**: Activates policies when premium payments complete
- **Logic**: Filters for PREMIUM payments and calls activation logic

### ClaimService
- **File**: `PaymentEventConsumer.java` 
- **Purpose**: Updates claim status when payments complete/fail
- **Logic**: Filters for CLAIM_PAYMENT events and updates accordingly

## For Beginners

### Key Concepts
1. **Producer**: Service that sends messages (PaymentService)
2. **Consumer**: Service that receives messages (PolicyService, ClaimService)
3. **Topic**: Message channel (`payment-events`)
4. **Avro**: Schema format for structured data
5. **Schema Registry**: Manages schema versions

### Simple Modifications
1. **Add Business Logic**: Replace TODO comments with actual service calls
2. **Error Handling**: Add retry logic and dead letter queues
3. **Monitoring**: Add metrics and health checks
4. **Testing**: Use embedded Kafka for unit tests

## Troubleshooting

### Common Issues
1. **Schema Registry Connection**: Check if running on port 8081
2. **Topic Not Found**: Ensure auto-creation is enabled
3. **Serialization Errors**: Verify Avro schema compatibility
4. **Consumer Not Receiving**: Check group IDs and offsets

### Debug Commands
```bash
# Check Kafka logs
docker logs kafka

# Check Schema Registry logs  
docker logs schema-registry

# Reset consumer group
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group policy-service --reset-offsets --to-earliest --topic payment-events --execute
```

## Next Steps
1. Add real business logic to TODO sections
2. Implement error handling and retries
3. Add monitoring and metrics
4. Write integration tests
5. Add more event types as needed 