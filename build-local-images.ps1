Write-Host "Building Insurance Project Docker Images for Local Deployment..." -ForegroundColor Green

# Build Config Server
Write-Host "Building Config Server..." -ForegroundColor Yellow
Set-Location ConfigServer
docker build -t insurance/config-server:latest .
Set-Location ..

# Build Service Registry (Eureka)
Write-Host "Building Service Registry..." -ForegroundColor Yellow
Set-Location service-registry
docker build -t insurance/service-registry:latest .
Set-Location ..

# Build Product Service
Write-Host "Building Product Service..." -ForegroundColor Yellow
Set-Location ProductService
docker build -t insurance/product-service:latest .
Set-Location ..

# Build Policy Service
Write-Host "Building Policy Service..." -ForegroundColor Yellow
Set-Location PolicyService
docker build -t insurance/policy-service:latest .
Set-Location ..

# Build Payment Service
Write-Host "Building Payment Service..." -ForegroundColor Yellow
Set-Location PaymentService
docker build -t insurance/payment-service:latest .
Set-Location ..

# Build Claim Service
Write-Host "Building Claim Service..." -ForegroundColor Yellow
Set-Location ClaimService
docker build -t insurance/claim-service:latest .
Set-Location ..

# Build Cloud Gateway
Write-Host "Building Cloud Gateway..." -ForegroundColor Yellow
Set-Location CloudGateway
docker build -t insurance/cloud-gateway:latest .
Set-Location ..

Write-Host "All Docker images built successfully!" -ForegroundColor Green
Write-Host "Images created:" -ForegroundColor Cyan
Write-Host "  - insurance/config-server:latest"
Write-Host "  - insurance/service-registry:latest"
Write-Host "  - insurance/product-service:latest"
Write-Host "  - insurance/policy-service:latest"
Write-Host "  - insurance/payment-service:latest"
Write-Host "  - insurance/claim-service:latest"
Write-Host "  - insurance/cloud-gateway:latest"

Write-Host ""
Write-Host "You can now deploy to Kubernetes using:" -ForegroundColor Green
Write-Host "kubectl apply -f K8s/" -ForegroundColor White 