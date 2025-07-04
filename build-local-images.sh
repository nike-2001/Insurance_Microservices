#!/bin/bash

echo "Building Insurance Project Docker Images for Local Deployment..."

# Build Config Server
echo "Building Config Server..."
cd ConfigServer
docker build -t insurance/config-server:latest .
cd ..

# Build Service Registry (Eureka)
echo "Building Service Registry..."
cd service-registry
docker build -t insurance/service-registry:latest .
cd ..

# Build Product Service
echo "Building Product Service..."
cd ProductService
docker build -t insurance/product-service:latest .
cd ..

# Build Policy Service
echo "Building Policy Service..."
cd PolicyService
docker build -t insurance/policy-service:latest .
cd ..

# Build Payment Service
echo "Building Payment Service..."
cd PaymentService
docker build -t insurance/payment-service:latest .
cd ..

# Build Claim Service
echo "Building Claim Service..."
cd ClaimService
docker build -t insurance/claim-service:latest .
cd ..

# Build Cloud Gateway
echo "Building Cloud Gateway..."
cd CloudGateway
docker build -t insurance/cloud-gateway:latest .
cd ..

echo "All Docker images built successfully!"
echo "Images created:"
echo "  - insurance/config-server:latest"
echo "  - insurance/service-registry:latest"
echo "  - insurance/product-service:latest"
echo "  - insurance/policy-service:latest"
echo "  - insurance/payment-service:latest"
echo "  - insurance/claim-service:latest"
echo "  - insurance/cloud-gateway:latest"

echo ""
echo "You can now deploy to Kubernetes using:"
echo "kubectl apply -f K8s/" 