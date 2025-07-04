# Complete Docker & Kubernetes Deployment Guide - Insurance Project

## Project Overview
This document contains all the steps we completed to deploy the Insurance microservices project using Docker and Kubernetes with Minikube.

## Architecture
- **ClaimService** (Port 8084)
- **PolicyService** (Port 8082) 
- **ProductService** (Port 8083)
- **PaymentService** (Port 8081)
- **CloudGateway** (Port 9090)
- **ConfigServer** (Port 9296)
- **ServiceRegistry/Eureka** (Port 8761)
- **MySQL Database** (Port 3306)
- **Redis** (Port 6379)
- **Zipkin** (Port 9411)

## Prerequisites Installed
- Docker Desktop
- Minikube
- kubectl CLI
- Maven (for building Java applications)

## Phase 1: Docker Setup

### 1.1 Build Maven Projects
```bash
# Build each service individually
cd service-registry && mvn clean package -DskipTests && cd ..
cd ConfigServer && mvn clean package -DskipTests && cd ..
cd CloudGateway && mvn clean package -DskipTests && cd ..
cd PolicyService && mvn clean package -DskipTests && cd ..
cd ProductService && mvn clean package -DskipTests && cd ..
cd PaymentService && mvn clean package -DskipTests && cd ..
cd ClaimService && mvn clean package -DskipTests && cd ..
```

### 1.2 Build Docker Images
```bash
# Build Docker images for each service
docker build -t nikhilkorrapati/serviceregistry:latest .
docker build -t nikhilkorrapati/configserver:latest .
docker build -t nikhilkorrapati/cloudgateway:latest .
docker build -t nikhilkorrapati/policyservice:latest .
docker build -t nikhilkorrapati/productservice:latest .
docker build -t nikhilkorrapati/paymentservice:latest .
docker build -t nikhilkorrapati/claimservice:latest .
```

### 1.3 Tag Docker Images
```bash
# Tag Docker images for each service
docker tag nikhilkorrapati/serviceregistry:latest nikhilkorrapati/serviceregistry:latest
docker tag nikhilspring/configserver:latest nikhilkorrapati/configserver:latest
docker tag nikhilkorrapati/cloudgateway:latest nikhilkorrapati/cloudgateway:latest
docker tag nikhilkorrapati/policyservice:latest nikhilkorrapati/policyservice:latest
docker tag nikhilkorrapati/productservice:latest nikhilkorrapati/productservice:latest
docker tag nikhilkorrapati/paymentservice:latest nikhilkorrapati/paymentservice:latest
docker tag nikhilkorrapati /claimservice:latest nikhilkorrapati/claimservice:latest
```

### 1.4 Push Images to Docker Hub
```bash
# Login to Docker Hub
docker login

# Push all images to Docker Hub
docker push nikhilkorrapati/serviceregistry:latest
docker push nikhilkorrapati/configserver:latest
docker push nikhilkorrapati/cloudgateway:latest
docker push nikhilkorrapati/policyservice:latest
docker push nikhilkorrapati/productservice:latest
docker push nikhilkorrapati/paymentservice:latest
docker push nikhilkorrapati/claimservice:latest
```

### 1.5 Docker Run Commands (Individual Service Testing)

#### Important Note for Windows Docker Desktop
When running containers individually on Windows Docker Desktop, use `host.docker.internal` instead of `localhost` to connect to services running on the host machine or other containers exposed on host ports.

#### Run MySQL Database
```bash
# Run MySQL container
docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=policydb \
  mysql:8.0

# Verify MySQL is running
docker ps | grep mysql
docker logs mysql
```

#### Run Service Registry (Eureka)
```bash
# Run Service Registry
docker run -d \
  --name serviceregistry \
  -p 8761:8761 \
  nikhilkorrapati/serviceregistry:latest

# Check logs
docker logs -f serviceregistry
```

#### Run Config Server
```bash
# Run Config Server
docker run -d \
  --name configserver \
  -p 9296:9296 \
  -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka \
  nikhilkorrapati/configserver:latest

# Check logs
docker logs -f configserver
```

#### Run Business Services (Windows Docker Desktop)
```bash
# Run Policy Service
docker run -d \
  --name policyservice \
  -p 8082:8082 \
  -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka \
  -e CONFIG_SERVER_URL=host.docker.internal \
  -e DB_HOST=host.docker.internal \
  nikhilkorrapati/policyservice:latest

# Run Product Service
docker run -d \
  --name productservice \
  -p 8083:8083 \
  -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka \
  -e CONFIG_SERVER_URL=host.docker.internal \
  -e DB_HOST=host.docker.internal \
  nikhilkorrapati/productservice:latest

# Run Payment Service
docker run -d \
  --name paymentservice \
  -p 8081:8081 \
  -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka \
  -e CONFIG_SERVER_URL=host.docker.internal \
  -e DB_HOST=host.docker.internal \
  nikhilkorrapati/paymentservice:latest

# Run Claim Service
docker run -d \
  --name claimservice \
  -p 8084:8084 \
  -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka \
  -e CONFIG_SERVER_URL=host.docker.internal \
  -e DB_HOST=host.docker.internal \
  nikhilkorrapati/claimservice:latest

# Run Cloud Gateway
docker run -d \
  --name cloudgateway \
  -p 9090:9090 \
  -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka \
  -e CONFIG_SERVER_URL=host.docker.internal \
  nikhilkorrapati/cloudgateway:latest
```

#### Docker Network (Recommended for Service Communication)
```bash
# Create custom network for better service communication
docker network create insurance-network

# Run services with custom network
docker run -d \
  --name mysql \
  --network insurance-network \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=policydb \
  mysql:8.0

docker run -d \
  --name serviceregistry \
  --network insurance-network \
  -p 8761:8761 \
  nikhilkorrapati/serviceregistry:latest

docker run -d \
  --name configserver \
  --network insurance-network \
  -p 9296:9296 \
  -e EUREKA_SERVER_ADDRESS=http://serviceregistry:8761/eureka \
  nikhilkorrapati/configserver:latest

docker run -d \
  --name policyservice \
  --network insurance-network \
  -p 8082:8082 \
  -e EUREKA_SERVER_ADDRESS=http://serviceregistry:8761/eureka \
  -e CONFIG_SERVER_URL=configserver \
  -e DB_HOST=mysql \
  nikhilkorrapati/policyservice:latest

# Continue with other services using --network insurance-network
```

#### Docker Management Commands
```bash
# List running containers
docker ps

# Stop all containers
docker stop mysql serviceregistry configserver policyservice productservice paymentservice claimservice cloudgateway

# Remove all containers
docker rm mysql serviceregistry configserver policyservice productservice paymentservice claimservice cloudgateway

# Remove custom network
docker network rm insurance-network

# Check container logs
docker logs -f <container-name>

# Execute into container
docker exec -it <container-name> bash

# Check container resource usage
docker stats
```

#### Docker Networking Troubleshooting
```bash
# Test connectivity from inside container (Windows)
docker exec -it policyservice ping host.docker.internal

# Test connectivity from inside container (Linux/Mac)
docker exec -it policyservice ping host.docker.internal
# Or use gateway IP: 
docker exec -it policyservice ping 172.17.0.1

# Check Docker network configuration
docker network ls
docker network inspect bridge

# Test service connectivity
curl http://host.docker.internal:8761  # From host
docker exec -it policyservice curl http://host.docker.internal:8761  # From container
```

#### Platform-Specific Configurations
```bash
# Windows Docker Desktop - Use host.docker.internal
-e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka
-e DB_HOST=host.docker.internal

# Linux Docker - Use host gateway IP (usually 172.17.0.1)
-e EUREKA_SERVER_ADDRESS=http://172.17.0.1:8761/eureka
-e DB_HOST=172.17.0.1

# Or use --add-host for custom host mapping
docker run --add-host=host.docker.internal:host-gateway ...

# Mac Docker Desktop - Use host.docker.internal (same as Windows)
-e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka
-e DB_HOST=host.docker.internal
```

### 1.6 Docker Compose Deployment (Local Development)
```bash
# Start all services with Docker Compose
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## Phase 2: Kubernetes Setup with Minikube

### 2.1 Start Minikube
```bash
# Start Minikube with sufficient resources
minikube start --memory=8192 --cpus=4 --disk-size=20g

# Enable required addons
minikube addons enable ingress
minikube addons enable metrics-server
minikube addons enable dashboard

# Verify Minikube status
minikube status
kubectl get nodes
```

### 2.2 Access Minikube Dashboard
```bash
# Start Minikube dashboard
minikube dashboard

# Or get dashboard URL
minikube dashboard --url
```

## Phase 3: Kubernetes Deployment

### 3.1 Create Namespace
```bash
# Apply namespace
kubectl apply -f K8s/namespace.yaml

# Verify namespace
kubectl get namespaces
```

### 3.2 Deploy ConfigMaps
```bash
# Apply ConfigMaps with correct service names
kubectl apply -f K8s/config-maps.yaml -n insurance-project

# Verify ConfigMaps
kubectl get configmaps -n insurance-project
kubectl describe configmap eureka-cm -n insurance-project
kubectl describe configmap mysql-cm -n insurance-project
kubectl describe configmap config-cm -n insurance-project
```

### 3.3 Deploy Infrastructure Services (In Order)

#### Deploy MySQL Database
```bash
kubectl apply -f K8s/mysql-deployment.yaml -n insurance-project

# Wait for MySQL to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n insurance-project --timeout=300s

# Check MySQL status
kubectl get pods -l app=mysql -n insurance-project
```

#### Deploy Service Registry (Eureka)
```bash
kubectl apply -f K8s/service-registry-statefulset.yaml -n insurance-project

# Wait for Service Registry
kubectl wait --for=condition=ready pod -l app=eureka -n insurance-project --timeout=300s

# Check Service Registry status
kubectl get pods -l app=eureka -n insurance-project
```

#### Deploy Config Server
```bash
kubectl apply -f K8s/config-server-deployment.yaml -n insurance-project

# Wait for Config Server
kubectl wait --for=condition=ready pod -l app=config-server-app -n insurance-project --timeout=300s

# Check Config Server status
kubectl get pods -l app=config-server-app -n insurance-project
```

### 3.4 Deploy Application Services

#### Deploy Cloud Gateway
```bash
kubectl apply -f K8s/cloud-gateway-deployment.yaml -n insurance-project

# Wait for Cloud Gateway
kubectl wait --for=condition=ready pod -l app=cloud-gateway-app -n insurance-project --timeout=300s
```

#### Deploy Business Services
```bash
# Deploy all business services
kubectl apply -f K8s/policy-service-deployment.yaml -n insurance-project
kubectl apply -f K8s/insurance-product-service-deployment.yaml -n insurance-project
kubectl apply -f K8s/payment-service-deployment.yaml -n insurance-project
kubectl apply -f K8s/claim-service-deployment.yaml -n insurance-project

# Wait for all business services
kubectl wait --for=condition=ready pod -l app=policy-service-app -n insurance-project --timeout=300s
kubectl wait --for=condition=ready pod -l app=product-service-app -n insurance-project --timeout=300s
kubectl wait --for=condition=ready pod -l app=payment-service-app -n insurance-project --timeout=300s
kubectl wait --for=condition=ready pod -l app=claim-service-app -n insurance-project --timeout=300s
```

#### Deploy Supporting Services
```bash
kubectl apply -f K8s/redis-deployment.yaml -n insurance-project
kubectl apply -f K8s/zipkin-deployment.yaml -n insurance-project

# Wait for supporting services
kubectl wait --for=condition=ready pod -l app=redis-app -n insurance-project --timeout=300s
kubectl wait --for=condition=ready pod -l app=zipkin-app -n insurance-project --timeout=300s
```

### 3.5 Deploy Ingress
```bash
kubectl apply -f K8s/ingress.yaml -n insurance-project

# Check ingress
kubectl get ingress -n insurance-project
```

### 3.6 Alternative - Deploy All at Once
```bash
# Deploy all resources to insurance-project namespace
kubectl apply -f K8s/ -n insurance-project
```

## Phase 4: Verification and Monitoring

### 4.1 Check Deployment Status
```bash
# Check all resources
kubectl get all -n insurance-project

# Check pod status
kubectl get pods -n insurance-project

# Check services
kubectl get services -n insurance-project

# Check deployments
kubectl get deployments -n insurance-project

# Check events for troubleshooting
kubectl get events -n insurance-project --sort-by='.lastTimestamp'
```

### 4.2 View Logs
```bash
# Check logs for specific services
kubectl logs -f deployment/claim-service-app -n insurance-project
kubectl logs -f deployment/policy-service-app -n insurance-project
kubectl logs -f deployment/payment-service-app -n insurance-project
kubectl logs -f deployment/product-service-app -n insurance-project

# Check infrastructure service logs
kubectl logs -f statefulset/eureka -n insurance-project
kubectl logs -f statefulset/mysql -n insurance-project
kubectl logs -f deployment/config-server-app -n insurance-project
kubectl logs -f deployment/cloud-gateway-app -n insurance-project

# Check logs by labels
kubectl logs -f -l app=claim-service-app -n insurance-project
```

### 4.3 Troubleshooting Commands
```bash
# Describe pods for detailed information
kubectl describe pods -n insurance-project

# Check resource usage
kubectl top pods -n insurance-project
kubectl top nodes

# Check endpoints
kubectl get endpoints -n insurance-project

# Check ConfigMaps
kubectl get configmaps -n insurance-project
```

## Phase 5: Testing Services

### 5.1 Port Forwarding for Testing

#### Test Cloud Gateway (Main Entry Point)
```bash
kubectl port-forward -n insurance-project svc/cloud-gateway-svc 9090:80

# Test health endpoint
curl http://localhost:9090/actuator/health
```

#### Test Service Registry Dashboard
```bash
kubectl port-forward -n insurance-project svc/eureka-lb 8761:80

# Access in browser: http://localhost:8761
# Or test via curl
curl http://localhost:8761/actuator/health
```

#### Test Individual Services
```bash
# Policy Service
kubectl port-forward -n insurance-project svc/policy-service-svc 8082:80
curl http://localhost:8082/actuator/health

# Product Service
kubectl port-forward -n insurance-project svc/product-service-svc 8083:80
curl http://localhost:8083/actuator/health

# Payment Service
kubectl port-forward -n insurance-project svc/payment-service-svc 8081:80
curl http://localhost:8081/actuator/health

# Claim Service
kubectl port-forward -n insurance-project svc/claim-service-svc 8084:80
curl http://localhost:8084/actuator/health
```

### 5.2 Test Through Cloud Gateway
```bash
# Port forward to Cloud Gateway
kubectl port-forward -n insurance-project svc/cloud-gateway-svc 9090:80

# Test all services through gateway
curl http://localhost:9090/policy/actuator/health
curl http://localhost:9090/product/actuator/health
curl http://localhost:9090/payment/actuator/health
curl http://localhost:9090/claim/actuator/health
```

### 5.3 Test Database Connectivity
```bash
# Port forward to MySQL
kubectl port-forward -n insurance-project svc/mysql 3306:3306

# Connect using MySQL client
mysql -h localhost -P 3306 -u root -proot

# Or test from within a pod
kubectl exec -it -n insurance-project deployment/policy-service-app -- bash
```

### 5.4 Using Minikube Service
```bash
# Get Minikube IP
minikube ip

# Access services via Minikube
minikube service eureka-lb -n insurance-project
minikube service cloud-gateway-svc -n insurance-project

# List all services
minikube service list -n insurance-project

# Use Minikube tunnel for external access
minikube tunnel
```

## Phase 6: Scaling and Management

### 6.1 Scale Services
```bash
# Scale claim service to 3 replicas
kubectl scale deployment claim-service-app --replicas=3 -n insurance-project

# Scale policy service to 2 replicas
kubectl scale deployment policy-service-app --replicas=2 -n insurance-project

# Check scaled pods
kubectl get pods -n insurance-project
```

### 6.2 Update Services
```bash
# Update an image
kubectl set image deployment/claim-service-app claim-service-app=nikhilkorrapati/claimservice:v2 -n insurance-project

# Check rollout status
kubectl rollout status deployment/claim-service-app -n insurance-project

# Rollback if needed
kubectl rollout undo deployment/claim-service-app -n insurance-project
```

### 6.3 Restart Services
```bash
# Restart deployments (useful after ConfigMap changes)
kubectl rollout restart deployment claim-service-app -n insurance-project
kubectl rollout restart deployment policy-service-app -n insurance-project
kubectl rollout restart deployment payment-service-app -n insurance-project
kubectl rollout restart deployment product-service-app -n insurance-project
```

## Configuration Files Fixed

### ConfigMaps Fixed
- **Eureka Service Address**: `http://eureka:8761/eureka` (was `http://eureka-0.eureka:8761/eureka`)
- **MySQL Hostname**: `mysql` (was `mysql-0.mysql`)
- **Config Server URL**: `http://config-server-svc`

### Namespace Configuration
- **Namespace**: `insurance-project` (consistent across all deployments)

## Key Lessons Learned

1. **Service Names**: Use Kubernetes service names for DNS resolution, not pod names
2. **Namespace Consistency**: Ensure all resources use the same namespace
3. **Deployment Order**: Infrastructure services (DB, Service Registry, Config Server) before business services
4. **Health Checks**: Always verify services are healthy before proceeding
5. **ConfigMaps**: Critical for service-to-service communication in Kubernetes

## Port Summary
- **Cloud Gateway**: 9090 (main entry point)
- **Service Registry**: 8761
- **Config Server**: 9296
- **Policy Service**: 8082
- **Product Service**: 8083
- **Payment Service**: 8081
- **Claim Service**: 8084
- **MySQL**: 3306
- **Redis**: 6379
- **Zipkin**: 9411

## Quick Commands Reference

```bash
# Start everything
minikube start --memory=8192 --cpus=4
kubectl apply -f K8s/ -n insurance-project

# Check status
kubectl get all -n insurance-project

# Access main application
kubectl port-forward -n insurance-project svc/cloud-gateway-svc 9090:80

# View logs
kubectl logs -f deployment/claim-service-app -n insurance-project

# Scale services
kubectl scale deployment claim-service-app --replicas=3 -n insurance-project

# Cleanup
kubectl delete namespace insurance-project
minikube stop
```

## Cleanup Commands

```bash
# Delete all Kubernetes resources
kubectl delete namespace insurance-project

# Stop Minikube
minikube stop

# Delete Minikube cluster (complete reset)
minikube delete

# Remove Docker images (if needed)
docker rmi nikhilkorrapati/serviceregistry:latest
docker rmi nikhilkorrapati/configserver:latest
docker rmi nikhilkorrapati/cloudgateway:latest
docker rmi nikhilkorrapati/policyservice:latest
docker rmi nikhilkorrapati/productservice:latest
docker rmi nikhilkorrapati/paymentservice:latest
docker rmi nikhilkorrapati/claimservice:latest
```

## Next Steps (Industry Standard)
1. **Monitoring**: Prometheus + Grafana
2. **CI/CD Pipeline**: Jenkins/GitHub Actions
3. **Helm Charts**: Package management
4. **Resource Limits**: CPU/Memory constraints
5. **Health Checks**: Liveness/Readiness probes
6. **Security**: RBAC, Network Policies
7. **Auto-scaling**: HPA (Horizontal Pod Autoscaler)

---

**Status**: ✅ Successfully deployed all microservices to Kubernetes with proper service discovery, configuration management, and networking. 