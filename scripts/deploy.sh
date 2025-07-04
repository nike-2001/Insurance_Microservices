#!/bin/bash

# Insurance Project Deployment Script
# This script handles deployment to different environments

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
K8S_DIR="$PROJECT_ROOT/K8s"

# Default values
ENVIRONMENT="dev"
NAMESPACE="insurance-project"
DOCKER_REGISTRY="nikhilkorrapati"
DOCKER_REGISTRY_SPRING="nikhilspring"
VERSION="latest"
DRY_RUN=false
ROLLBACK=false

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
Insurance Project Deployment Script

Usage: $0 [OPTIONS]

Options:
    -e, --environment ENV    Deployment environment (dev|staging|prod) [default: dev]
    -n, --namespace NS       Kubernetes namespace [default: insurance-project]
    -v, --version VERSION    Docker image version [default: latest]
    -r, --registry REGISTRY  Docker registry [default: nikhilkorrapati]
    -d, --dry-run           Perform dry run without actual deployment
    --rollback              Rollback to previous version
    -h, --help              Show this help message

Examples:
    $0 -e dev -v v1.0.0
    $0 -e staging -n insurance-project-staging
    $0 -e prod --dry-run
    $0 --rollback -e prod

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -v|--version)
            VERSION="$2"
            shift 2
            ;;
        -r|--registry)
            DOCKER_REGISTRY="$2"
            shift 2
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        --rollback)
            ROLLBACK=true
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

# Validate environment
case $ENVIRONMENT in
    dev|development)
        ENVIRONMENT="dev"
        NAMESPACE="${NAMESPACE:-insurance-project}"
        ;;
    staging|stage)
        ENVIRONMENT="staging"
        NAMESPACE="${NAMESPACE:-insurance-project-staging}"
        ;;
    prod|production)
        ENVIRONMENT="prod"
        NAMESPACE="${NAMESPACE:-insurance-project-prod}"
        ;;
    *)
        log_error "Invalid environment: $ENVIRONMENT. Must be dev, staging, or prod"
        exit 1
        ;;
esac

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed"
        exit 1
    fi
    
    # Check if kubectl can connect to cluster
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    # Check if K8s directory exists
    if [[ ! -d "$K8S_DIR" ]]; then
        log_error "Kubernetes manifests directory not found: $K8S_DIR"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Create namespace if it doesn't exist
create_namespace() {
    log_info "Creating namespace: $NAMESPACE"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_warning "DRY RUN: Would create namespace $NAMESPACE"
        return
    fi
    
    kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
    log_success "Namespace $NAMESPACE is ready"
}

# Update image versions in Kubernetes manifests
update_image_versions() {
    log_info "Updating image versions to: $VERSION"
    
    local temp_dir=$(mktemp -d)
    cp -r "$K8S_DIR"/* "$temp_dir/"
    
    # Update image versions in deployment files
    find "$temp_dir" -name "*-deployment.yaml" -type f | while read -r file; do
        log_info "Updating $file"
        
        # Update image versions based on service
        case $(basename "$file") in
            "claim-service-deployment.yaml")
                sed -i "s|image: $DOCKER_REGISTRY/claimservice:.*|image: $DOCKER_REGISTRY/claimservice:$VERSION|g" "$file"
                ;;
            "policy-service-deployment.yaml")
                sed -i "s|image: $DOCKER_REGISTRY/policyservice:.*|image: $DOCKER_REGISTRY/policyservice:$VERSION|g" "$file"
                ;;
            "product-service-deployment.yaml")
                sed -i "s|image: $DOCKER_REGISTRY/productservice:.*|image: $DOCKER_REGISTRY/productservice:$VERSION|g" "$file"
                ;;
            "payment-service-deployment.yaml")
                sed -i "s|image: $DOCKER_REGISTRY/paymentservice:.*|image: $DOCKER_REGISTRY/paymentservice:$VERSION|g" "$file"
                ;;
            "cloud-gateway-deployment.yaml")
                sed -i "s|image: $DOCKER_REGISTRY_SPRING/cloudgateway:.*|image: $DOCKER_REGISTRY_SPRING/cloudgateway:$VERSION|g" "$file"
                ;;
            "config-server-deployment.yaml")
                sed -i "s|image: $DOCKER_REGISTRY_SPRING/configserver:.*|image: $DOCKER_REGISTRY_SPRING/configserver:$VERSION|g" "$file"
                ;;
            "service-registry-statefulset.yaml")
                sed -i "s|image: $DOCKER_REGISTRY_SPRING/serviceregistry:.*|image: $DOCKER_REGISTRY_SPRING/serviceregistry:$VERSION|g" "$file"
                ;;
        esac
    done
    
    echo "$temp_dir"
}

# Deploy to Kubernetes
deploy_to_kubernetes() {
    local manifests_dir="$1"
    
    log_info "Deploying to Kubernetes namespace: $NAMESPACE"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_warning "DRY RUN: Would deploy to namespace $NAMESPACE"
        kubectl apply -f "$manifests_dir" -n "$NAMESPACE" --dry-run=client
        return
    fi
    
    # Apply all manifests
    kubectl apply -f "$manifests_dir" -n "$NAMESPACE"
    
    # Wait for deployments to be ready
    log_info "Waiting for deployments to be ready..."
    
    # Wait for MySQL
    kubectl rollout status deployment/mysql -n "$NAMESPACE" --timeout=300s
    
    # Wait for Eureka
    kubectl rollout status statefulset/eureka -n "$NAMESPACE" --timeout=300s
    
    # Wait for Config Server
    kubectl rollout status deployment/config-server-app -n "$NAMESPACE" --timeout=300s
    
    # Wait for Cloud Gateway
    kubectl rollout status deployment/cloud-gateway-app -n "$NAMESPACE" --timeout=300s
    
    # Wait for microservices
    kubectl rollout status deployment/policy-service-app -n "$NAMESPACE" --timeout=300s
    kubectl rollout status deployment/product-service-app -n "$NAMESPACE" --timeout=300s
    kubectl rollout status deployment/payment-service-app -n "$NAMESPACE" --timeout=300s
    kubectl rollout status deployment/claim-service-app -n "$NAMESPACE" --timeout=300s
    
    log_success "All deployments are ready"
}

# Rollback deployment
rollback_deployment() {
    log_info "Rolling back deployment in namespace: $NAMESPACE"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_warning "DRY RUN: Would rollback deployments"
        return
    fi
    
    # Rollback all deployments
    kubectl rollout undo deployment/mysql -n "$NAMESPACE"
    kubectl rollout undo statefulset/eureka -n "$NAMESPACE"
    kubectl rollout undo deployment/config-server-app -n "$NAMESPACE"
    kubectl rollout undo deployment/cloud-gateway-app -n "$NAMESPACE"
    kubectl rollout undo deployment/policy-service-app -n "$NAMESPACE"
    kubectl rollout undo deployment/product-service-app -n "$NAMESPACE"
    kubectl rollout undo deployment/payment-service-app -n "$NAMESPACE"
    kubectl rollout undo deployment/claim-service-app -n "$NAMESPACE"
    
    log_success "Rollback completed"
}

# Health check
health_check() {
    log_info "Performing health check..."
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_warning "DRY RUN: Would perform health check"
        return
    fi
    
    # Get service URLs
    local gateway_url=$(kubectl get svc cloud-gateway-svc -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    
    if [[ -z "$gateway_url" ]]; then
        log_warning "LoadBalancer IP not available, using port-forward"
        # Use port-forward for local testing
        kubectl port-forward svc/cloud-gateway-svc 9090:80 -n "$NAMESPACE" &
        local port_forward_pid=$!
        sleep 5
        gateway_url="localhost:9090"
    fi
    
    # Health check endpoints
    local endpoints=(
        "http://$gateway_url/actuator/health"
        "http://$gateway_url/policy-service/actuator/health"
        "http://$gateway_url/product-service/actuator/health"
        "http://$gateway_url/payment-service/actuator/health"
        "http://$gateway_url/claim-service/actuator/health"
    )
    
    for endpoint in "${endpoints[@]}"; do
        log_info "Checking: $endpoint"
        if curl -f -s "$endpoint" > /dev/null; then
            log_success "Health check passed: $endpoint"
        else
            log_error "Health check failed: $endpoint"
            if [[ -n "$port_forward_pid" ]]; then
                kill "$port_forward_pid" 2>/dev/null || true
            fi
            exit 1
        fi
    done
    
    if [[ -n "$port_forward_pid" ]]; then
        kill "$port_forward_pid" 2>/dev/null || true
    fi
    
    log_success "All health checks passed"
}

# Cleanup function
cleanup() {
    if [[ -n "$TEMP_DIR" && -d "$TEMP_DIR" ]]; then
        rm -rf "$TEMP_DIR"
    fi
}

# Main execution
main() {
    log_info "Starting deployment to $ENVIRONMENT environment"
    log_info "Namespace: $NAMESPACE"
    log_info "Version: $VERSION"
    log_info "Registry: $DOCKER_REGISTRY"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_warning "DRY RUN MODE - No actual changes will be made"
    fi
    
    # Set trap for cleanup
    trap cleanup EXIT
    
    # Check prerequisites
    check_prerequisites
    
    # Create namespace
    create_namespace
    
    if [[ "$ROLLBACK" == "true" ]]; then
        rollback_deployment
    else
        # Update image versions
        TEMP_DIR=$(update_image_versions)
        
        # Deploy to Kubernetes
        deploy_to_kubernetes "$TEMP_DIR"
        
        # Health check
        health_check
    fi
    
    log_success "Deployment completed successfully!"
}

# Run main function
main "$@" 