# Local Kubernetes Deployment Guide
## Insurance Project - Generic Local Deployment

This guide will help you deploy the Insurance Project microservices on any local Kubernetes cluster (Docker Desktop, Minikube, Kind, etc.) without AWS dependencies.

## Prerequisites

1. **Docker** - Ensure Docker is installed and running
2. **Kubernetes** - Local cluster (Docker Desktop K8s, Minikube, or Kind)
3. **kubectl** - Kubernetes command-line tool

## Step 1: Build Docker Images Locally

### For Windows (PowerShell):
```powershell
.\build-local-images.ps1
```

### For Linux/Mac (Bash):
```bash
chmod +x build-local-images.sh
./build-local-images.sh
```

### Manual Build (if scripts don't work):
```bash
# Build each service individually
docker build -t insurance/config-server:latest ./ConfigServer
docker build -t insurance/service-registry:latest ./service-registry
docker build -t insurance/product-service:latest ./ProductService
docker build -t insurance/policy-service:latest ./PolicyService
docker build -t insurance/payment-service:latest ./PaymentService
docker build -t insurance/claim-service:latest ./ClaimService
docker build -t insurance/cloud-gateway:latest ./CloudGateway
```

## Step 2: Verify Images Built
```bash
docker images | grep insurance
```

You should see all 7 images:
- insurance/config-server:latest
- insurance/service-registry:latest
- insurance/product-service:latest
- insurance/policy-service:latest
- insurance/payment-service:latest
- insurance/claim-service:latest
- insurance/cloud-gateway:latest

## Step 3: Deploy to Kubernetes (In Order)

### 3.1 Create Namespace
```bash
kubectl apply -f K8s/namespace.yaml
```

### 3.2 Deploy ConfigMaps
```bash
kubectl apply -f K8s/config-maps.yaml
```

### 3.3 Deploy Storage Layer
```bash
# Deploy MySQL
kubectl apply -f K8s/mysql-deployment.yaml

# Deploy Redis
kubectl apply -f K8s/redis-deployment.yaml
```

### 3.4 Deploy Service Registry
```bash
kubectl apply -f K8s/service-registry-statefulset.yaml
```

### 3.5 Deploy Config Server
```bash
kubectl apply -f K8s/config-server-deployment.yaml

# Wait for config server to be ready
kubectl wait --for=condition=ready pod -l app=config-server-app -n insurance-project --timeout=300s
```

### 3.6 Deploy Core Microservices
```bash
# Deploy Product Service
kubectl apply -f K8s/insurance-product-service-deployment.yaml

# Deploy Policy Service
kubectl apply -f K8s/policy-service-deployment.yaml

# Deploy Payment Service
kubectl apply -f K8s/payment-service-deployment.yaml

# Deploy Claim Service
kubectl apply -f K8s/claim-service-deployment.yaml
```

### 3.7 Deploy API Gateway
```bash
kubectl apply -f K8s/cloud-gateway-deployment.yaml
```

### 3.8 Deploy Monitoring
```bash
kubectl apply -f K8s/zipkin-deployment.yaml
```

## Step 4: Verify Deployment

### Check Pods Status
```bash
kubectl get pods -n insurance-project
```

All pods should show `Running` status.

### Check Services
```bash
kubectl get svc -n insurance-project
```

### Check Detailed Pod Information (if issues)
```bash
kubectl describe pod <pod-name> -n insurance-project
kubectl logs <pod-name> -n insurance-project
```

## Step 5: Access the Application

### Option 1: Port Forward
```bash
# Access Cloud Gateway
kubectl port-forward svc/cloud-gateway-svc 8080:80 -n insurance-project

# Access Eureka Dashboard
kubectl port-forward svc/eureka-lb 8761:80 -n insurance-project

# Access Zipkin
kubectl port-forward svc/zipkin-lb-svc 9411:9411 -n insurance-project
```

Then access:
- Application: http://localhost:8080
- Eureka: http://localhost:8761
- Zipkin: http://localhost:9411

### Option 2: NodePort (if using Minikube)
```bash
# Get NodePort URL
minikube service cloud-gateway-svc -n insurance-project --url
```

## Step 6: Optional - Install Ingress (Advanced)

### For Docker Desktop:
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```

### For Minikube:
```bash
minikube addons enable ingress
```

Then deploy ingress:
```bash
kubectl apply -f K8s/ingress.yaml
```

## Troubleshooting

### Common Issues:

1. **Image Pull Errors**: Make sure you built all images locally
2. **Pod Pending**: Check if namespace exists and resources are available
3. **Connection Refused**: Services might not be ready yet, wait a few minutes

### Useful Commands:
```bash
# Check all resources
kubectl get all -n insurance-project

# Check events
kubectl get events -n insurance-project --sort-by='.lastTimestamp'

# Restart deployment
kubectl rollout restart deployment <deployment-name> -n insurance-project

# Clean up everything
kubectl delete namespace insurance-project
```

## Application URLs (with Port Forward)

- **Main Application**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Zipkin Tracing**: http://localhost:9411
- **Individual Services**:
  - Config Server: http://localhost:9296 (port-forward required)
  - Product Service: http://localhost:8083 (port-forward required)
  - Policy Service: http://localhost:8082 (port-forward required)
  - Payment Service: http://localhost:8081 (port-forward required)
  - Claim Service: http://localhost:8084 (port-forward required)

Your insurance microservices application is now running locally on Kubernetes without any AWS dependencies! 