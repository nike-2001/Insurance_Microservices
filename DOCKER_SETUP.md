# Insurance Project - Docker Setup Guide

## Overview
This project consists of multiple microservices that need to be built and run using Docker Compose.

## Prerequisites
- Docker Desktop installed and running
- Docker Compose installed
- At least 4GB of available RAM
- Ports 8081-8084, 8761, 9090, 9296, and 3307 available

## Quick Start

### Option 1: Using the Windows Batch File (Recommended)
```bash
# Simply double-click or run:
start-services.bat
```

### Option 2: Using Docker Compose Commands
```bash
# Build and start all services
docker-compose up --build -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Restart services
docker-compose restart
```

## Service Architecture

### Core Infrastructure Services
- **Service Registry (Eureka)**: `http://localhost:8761`
  - Service discovery and registration
  - Health monitoring dashboard

- **Config Server**: `http://localhost:9296`
  - Centralized configuration management
  - Configuration refresh endpoints

- **Cloud Gateway**: `http://localhost:9090`
  - API Gateway and routing
  - Authentication and authorization
  - Rate limiting and circuit breaking

### Business Services
- **Policy Service**: `http://localhost:8082`
  - Policy management and operations
  - Policy validation and processing

- **Product Service**: `http://localhost:8083`
  - Insurance product catalog
  - Product information and pricing

- **Payment Service**: `http://localhost:8081`
  - Payment processing and transactions
  - Payment status tracking

- **Claim Service**: `http://localhost:8084`
  - Claim processing and validation
  - Claim status management

### Database
- **MySQL Database**: `localhost:3307`
  - External access port: 3307 (to avoid conflicts with local MySQL)
  - Internal container port: 3306
  - Database: `policydb`
  - Username: `root`
  - Password: `root`

## Port Configuration

| Service | External Port | Internal Port | Purpose |
|---------|---------------|---------------|---------|
| Service Registry | 8761 | 8761 | Eureka Server |
| Config Server | 9296 | 9296 | Configuration Management |
| Cloud Gateway | 9090 | 9090 | API Gateway |
| Policy Service | 8082 | 8082 | Policy Management |
| Product Service | 8083 | 8083 | Product Catalog |
| Payment Service | 8081 | 8081 | Payment Processing |
| Claim Service | 8084 | 8084 | Claim Processing |
| MySQL | 3307 | 3306 | Database |

## Important Notes

### Port 3307 for MySQL
The MySQL container runs on port 3307 externally to avoid conflicts with any local MySQL installation. The services inside the Docker network still connect to MySQL on port 3306.

### Service Dependencies
Services start in the following order:
1. MySQL (with health check)
2. Service Registry (depends on MySQL)
3. Config Server (depends on Service Registry)
4. All other services (depend on Config Server)

### Health Checks
All services include health checks to ensure proper startup order and service availability.

## Troubleshooting

### Port Already in Use
If you get a "port already in use" error:
1. Check what's using the port: `netstat -ano | findstr :PORT_NUMBER`
2. Stop the conflicting service or change the port in `docker-compose.yml`

### Build Failures
If services fail to build:
1. Ensure Docker has enough memory (at least 4GB)
2. Check that all Dockerfiles exist in their respective directories
3. Verify that Maven dependencies are properly configured

### Service Startup Issues
If services fail to start:
1. Check logs: `docker-compose logs SERVICE_NAME`
2. Ensure all dependencies are healthy
3. Verify database connectivity

### Database Connection Issues
If services can't connect to the database:
1. Ensure MySQL container is healthy: `docker-compose ps`
2. Check MySQL logs: `docker-compose logs mysql`
3. Verify the database initialization script ran successfully

## Development Workflow

### Making Changes
1. Make your code changes
2. Rebuild the specific service: `docker-compose build SERVICE_NAME`
3. Restart the service: `docker-compose restart SERVICE_NAME`

### Viewing Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f SERVICE_NAME

# Last 100 lines
docker-compose logs --tail=100 SERVICE_NAME
```

### Accessing Services
- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:9296
- **API Gateway**: http://localhost:9090
- **Individual Services**: Use their respective ports

## Cleanup
To completely clean up the environment:
```bash
# Stop and remove containers
docker-compose down

# Remove volumes (this will delete all data)
docker-compose down -v

# Remove images
docker-compose down --rmi all
``` 