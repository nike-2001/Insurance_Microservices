#!/bin/bash

# Insurance Project - Build and Run Script
# This script builds all services and starts them using Docker Compose

set -e  # Exit on any error

echo "🚀 Starting Insurance Project Build and Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

print_status "Building all services..."

# Build all services
docker-compose build --no-cache

if [ $? -eq 0 ]; then
    print_success "All services built successfully!"
else
    print_error "Failed to build services. Please check the error messages above."
    exit 1
fi

print_status "Starting all services..."

# Start all services
docker-compose up -d

if [ $? -eq 0 ]; then
    print_success "All services started successfully!"
    echo ""
    print_status "Service URLs:"
    echo "  - Service Registry: http://localhost:8761"
    echo "  - Config Server: http://localhost:9296"
    echo "  - Cloud Gateway: http://localhost:9090"
    echo "  - Policy Service: http://localhost:8082"
    echo "  - Product Service: http://localhost:8083"
    echo "  - Payment Service: http://localhost:8081"
    echo "  - Claim Service: http://localhost:8084"
    echo "  - MySQL Database: localhost:3306"
    echo ""
    print_status "To view logs: docker-compose logs -f"
    print_status "To stop services: docker-compose down"
    print_status "To restart services: docker-compose restart"
else
    print_error "Failed to start services. Please check the error messages above."
    exit 1
fi 