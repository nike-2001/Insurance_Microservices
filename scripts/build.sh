#!/bin/bash

# Insurance Project Build Script
# This script handles building all microservices

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Default values
BUILD_TYPE="all"
SERVICE=""
SKIP_TESTS=false
SKIP_DOCKER=false
DOCKER_REGISTRY="nikhilkorrapati"
DOCKER_REGISTRY_SPRING="nikhilspring"
VERSION="latest"
PUSH_IMAGES=false

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Help function
show_help() {
    cat << EOF
Insurance Project Build Script

Usage: $0 [OPTIONS]

Options:
    -t, --type TYPE          Build type (all|service|docker) [default: all]
    -s, --service SERVICE    Specific service to build
    --skip-tests            Skip running tests
    --skip-docker           Skip Docker image building
    -v, --version VERSION   Docker image version [default: latest]
    -r, --registry REGISTRY Docker registry [default: nikhilkorrapati]
    --push                  Push Docker images to registry
    -h, --help              Show this help message

Services:
    serviceregistry, configserver, cloudgateway, policyservice, 
    productservice, paymentservice, claimservice

Examples:
    $0 -t all -v v1.0.0
    $0 -t service -s policyservice
    $0 -t docker --push
    $0 --skip-tests --skip-docker

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--type)
            BUILD_TYPE="$2"
            shift 2
            ;;
        -s|--service)
            SERVICE="$2"
            shift 2
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --skip-docker)
            SKIP_DOCKER=true
            shift
            ;;
        -v|--version)
            VERSION="$2"
            shift 2
            ;;
        -r|--registry)
            DOCKER_REGISTRY="$2"
            shift 2
            ;;
        --push)
            PUSH_IMAGES=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Validate build type
case $BUILD_TYPE in
    all|service|docker)
        ;;
    *)
        log_error "Invalid build type: $BUILD_TYPE. Must be all, service, or docker"
        exit 1
        ;;
esac

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed"
        exit 1
    fi
    
    # Check Java version
    local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [[ "$java_version" != "21" ]]; then
        log_warning "Java version $java_version detected. Recommended: Java 21"
    fi
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed"
        exit 1
    fi
    
    # Check if Docker is installed (if building Docker images)
    if [[ "$SKIP_DOCKER" == "false" ]]; then
        if ! command -v docker &> /dev/null; then
            log_error "Docker is not installed"
            exit 1
        fi
        
        # Check if Docker daemon is running
        if ! docker info &> /dev/null; then
            log_error "Docker daemon is not running"
            exit 1
        fi
    fi
    
    log_success "Prerequisites check passed"
}

# Build a single service
build_service() {
    local service_name="$1"
    local service_dir="$PROJECT_ROOT/$service_name"
    
    if [[ ! -d "$service_dir" ]]; then
        log_error "Service directory not found: $service_dir"
        return 1
    fi
    
    log_info "Building service: $service_name"
    
    cd "$service_dir"
    
    # Clean and compile
    if [[ "$SKIP_TESTS" == "true" ]]; then
        log_info "Skipping tests for $service_name"
        mvn clean compile -DskipTests
        mvn package -DskipTests
    else
        log_info "Running tests for $service_name"
        mvn clean compile test
        mvn package -DskipTests
    fi
    
    # Check if JAR file was created
    local jar_file=$(find target -name "*.jar" -not -name "*sources.jar" -not -name "*javadoc.jar" | head -n 1)
    if [[ -n "$jar_file" ]]; then
        log_success "Service $service_name built successfully: $jar_file"
    else
        log_error "No JAR file found for service $service_name"
        return 1
    fi
    
    cd "$PROJECT_ROOT"
}

# Build all services
build_all_services() {
    log_info "Building all services..."
    
    local services=(
        "service-registry"
        "ConfigServer"
        "CloudGateway"
        "PolicyService"
        "ProductService"
        "PaymentService"
        "ClaimService"
    )
    
    local failed_services=()
    
    for service in "${services[@]}"; do
        if ! build_service "$service"; then
            failed_services+=("$service")
        fi
    done
    
    if [[ ${#failed_services[@]} -gt 0 ]]; then
        log_error "Failed to build services: ${failed_services[*]}"
        return 1
    fi
    
    log_success "All services built successfully"
}

# Build Docker image for a service
build_docker_image() {
    local service_name="$1"
    local service_dir="$PROJECT_ROOT/$service_name"
    
    if [[ ! -d "$service_dir" ]]; then
        log_error "Service directory not found: $service_dir"
        return 1
    fi
    
    if [[ ! -f "$service_dir/Dockerfile" ]]; then
        log_error "Dockerfile not found for service: $service_name"
        return 1
    fi
    
    log_info "Building Docker image for: $service_name"
    
    cd "$service_dir"
    
    # Determine registry and image name
    local registry="$DOCKER_REGISTRY"
    local image_name="$service_name"
    
    case $service_name in
        "service-registry")
            registry="$DOCKER_REGISTRY_SPRING"
            image_name="serviceregistry"
            ;;
        "ConfigServer")
            registry="$DOCKER_REGISTRY_SPRING"
            image_name="configserver"
            ;;
        "CloudGateway")
            registry="$DOCKER_REGISTRY_SPRING"
            image_name="cloudgateway"
            ;;
        "PolicyService")
            image_name="policyservice"
            ;;
        "ProductService")
            image_name="productservice"
            ;;
        "PaymentService")
            image_name="paymentservice"
            ;;
        "ClaimService")
            image_name="claimservice"
            ;;
    esac
    
    # Build Docker image
    local image_tag="$registry/$image_name:$VERSION"
    local latest_tag="$registry/$image_name:latest"
    
    log_info "Building image: $image_tag"
    docker build -t "$image_tag" -t "$latest_tag" .
    
    if [[ "$PUSH_IMAGES" == "true" ]]; then
        log_info "Pushing image: $image_tag"
        docker push "$image_tag"
        docker push "$latest_tag"
    fi
    
    log_success "Docker image built successfully: $image_tag"
    
    cd "$PROJECT_ROOT"
}

# Build all Docker images
build_all_docker_images() {
    log_info "Building all Docker images..."
    
    local services=(
        "service-registry"
        "ConfigServer"
        "CloudGateway"
        "PolicyService"
        "ProductService"
        "PaymentService"
        "ClaimService"
    )
    
    local failed_services=()
    
    for service in "${services[@]}"; do
        if ! build_docker_image "$service"; then
            failed_services+=("$service")
        fi
    done
    
    if [[ ${#failed_services[@]} -gt 0 ]]; then
        log_error "Failed to build Docker images for services: ${failed_services[*]}"
        return 1
    fi
    
    log_success "All Docker images built successfully"
}

# Main execution
main() {
    log_info "Starting build process"
    log_info "Build type: $BUILD_TYPE"
    log_info "Version: $VERSION"
    log_info "Registry: $DOCKER_REGISTRY"
    
    if [[ "$SKIP_TESTS" == "true" ]]; then
        log_warning "Tests will be skipped"
    fi
    
    if [[ "$SKIP_DOCKER" == "true" ]]; then
        log_warning "Docker image building will be skipped"
    fi
    
    if [[ "$PUSH_IMAGES" == "true" ]]; then
        log_info "Docker images will be pushed to registry"
    fi
    
    # Check prerequisites
    check_prerequisites
    
    # Execute build based on type
    case $BUILD_TYPE in
        "all")
            if [[ "$SKIP_DOCKER" == "false" ]]; then
                build_all_services
                build_all_docker_images
            else
                build_all_services
            fi
            ;;
        "service")
            if [[ -n "$SERVICE" ]]; then
                build_service "$SERVICE"
                if [[ "$SKIP_DOCKER" == "false" ]]; then
                    build_docker_image "$SERVICE"
                fi
            else
                build_all_services
            fi
            ;;
        "docker")
            if [[ -n "$SERVICE" ]]; then
                build_docker_image "$SERVICE"
            else
                build_all_docker_images
            fi
            ;;
    esac
    
    log_success "Build process completed successfully!"
}

# Run main function
main "$@" 