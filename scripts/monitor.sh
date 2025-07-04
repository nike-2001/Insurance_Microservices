#!/bin/bash

# Insurance Project Monitoring Script
# This script handles monitoring and health checks for deployed services

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Default values
NAMESPACE="insurance-project"
CHECK_TYPE="health"
TIMEOUT=30
RETRIES=3
VERBOSE=false
ALERT_ON_FAILURE=false
SLACK_WEBHOOK=""

# Service endpoints
declare -A SERVICE_ENDPOINTS=(
    ["service-registry"]="http://localhost:8761/actuator/health"
    ["config-server"]="http://localhost:9296/actuator/health"
    ["cloud-gateway"]="http://localhost:9090/actuator/health"
    ["policy-service"]="http://localhost:8082/actuator/health"
    ["product-service"]="http://localhost:8083/actuator/health"
    ["payment-service"]="http://localhost:8081/actuator/health"
    ["claim-service"]="http://localhost:8084/actuator/health"
)

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
Insurance Project Monitoring Script

Usage: $0 [OPTIONS]

Options:
    -n, --namespace NS       Kubernetes namespace [default: insurance-project]
    -t, --type TYPE          Check type (health|metrics|logs) [default: health]
    --timeout SECONDS        Timeout for health checks [default: 30]
    --retries COUNT          Number of retries [default: 3]
    -v, --verbose            Verbose output
    --alert                  Send alerts on failure
    --slack-webhook URL      Slack webhook URL for alerts
    -h, --help               Show this help message

Check Types:
    health    - Check service health endpoints
    metrics   - Check service metrics
    logs      - Check service logs for errors
    all       - Run all checks

Examples:
    $0 -t health -n insurance-project
    $0 -t all --alert --slack-webhook https://hooks.slack.com/...
    $0 -t metrics --verbose

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -t|--type)
            CHECK_TYPE="$2"
            shift 2
            ;;
        --timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        --retries)
            RETRIES="$2"
            shift 2
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        --alert)
            ALERT_ON_FAILURE=true
            shift
            ;;
        --slack-webhook)
            SLACK_WEBHOOK="$2"
            shift 2
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

# Validate check type
case $CHECK_TYPE in
    health|metrics|logs|all)
        ;;
    *)
        log_error "Invalid check type: $CHECK_TYPE. Must be health, metrics, logs, or all"
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
    
    # Check if curl is installed
    if ! command -v curl &> /dev/null; then
        log_error "curl is not installed"
        exit 1
    fi
    
    # Check if jq is installed (for JSON parsing)
    if ! command -v jq &> /dev/null; then
        log_warning "jq is not installed. JSON parsing will be limited."
    fi
    
    log_success "Prerequisites check passed"
}

# Get service URLs from Kubernetes
get_service_urls() {
    log_info "Getting service URLs from Kubernetes..."
    
    # Get LoadBalancer IPs or use port-forward
    local gateway_ip=$(kubectl get svc cloud-gateway-svc -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
    
    if [[ -n "$gateway_ip" ]]; then
        log_info "Using LoadBalancer IP: $gateway_ip"
        SERVICE_ENDPOINTS["cloud-gateway"]="http://$gateway_ip/actuator/health"
        SERVICE_ENDPOINTS["policy-service"]="http://$gateway_ip/policy-service/actuator/health"
        SERVICE_ENDPOINTS["product-service"]="http://$gateway_ip/product-service/actuator/health"
        SERVICE_ENDPOINTS["payment-service"]="http://$gateway_ip/payment-service/actuator/health"
        SERVICE_ENDPOINTS["claim-service"]="http://$gateway_ip/claim-service/actuator/health"
    else
        log_warning "LoadBalancer IP not available, using localhost with port-forward"
        # Start port-forward for monitoring
        kubectl port-forward svc/cloud-gateway-svc 9090:80 -n "$NAMESPACE" &
        local port_forward_pid=$!
        sleep 5
        echo "$port_forward_pid"
    fi
}

# Check service health
check_service_health() {
    local service_name="$1"
    local endpoint="$2"
    local port_forward_pid="$3"
    
    log_info "Checking health for: $service_name"
    
    local retry_count=0
    local success=false
    
    while [[ $retry_count -lt $RETRIES && "$success" == "false" ]]; do
        if [[ $retry_count -gt 0 ]]; then
            log_warning "Retry $retry_count for $service_name"
            sleep 2
        fi
        
        if curl -f -s --max-time "$TIMEOUT" "$endpoint" > /dev/null; then
            log_success "Health check passed for $service_name"
            success=true
        else
            log_error "Health check failed for $service_name (attempt $((retry_count + 1)))"
            retry_count=$((retry_count + 1))
        fi
    done
    
    if [[ "$success" == "false" ]]; then
        log_error "Health check failed for $service_name after $RETRIES retries"
        return 1
    fi
    
    # Get detailed health information if verbose
    if [[ "$VERBOSE" == "true" ]]; then
        local health_info=$(curl -s --max-time "$TIMEOUT" "$endpoint")
        if command -v jq &> /dev/null; then
            echo "$health_info" | jq .
        else
            echo "$health_info"
        fi
    fi
}

# Check service metrics
check_service_metrics() {
    local service_name="$1"
    local endpoint="$2"
    
    log_info "Checking metrics for: $service_name"
    
    # Convert health endpoint to metrics endpoint
    local metrics_endpoint=$(echo "$endpoint" | sed 's|/actuator/health|/actuator/metrics|')
    
    if curl -f -s --max-time "$TIMEOUT" "$metrics_endpoint" > /dev/null; then
        log_success "Metrics endpoint accessible for $service_name"
        
        if [[ "$VERBOSE" == "true" ]]; then
            local metrics=$(curl -s --max-time "$TIMEOUT" "$metrics_endpoint")
            echo "$metrics"
        fi
    else
        log_error "Metrics endpoint not accessible for $service_name"
        return 1
    fi
}

# Check service logs
check_service_logs() {
    local service_name="$1"
    
    log_info "Checking logs for: $service_name"
    
    # Convert service name to Kubernetes deployment name
    local deployment_name=""
    case $service_name in
        "service-registry")
            deployment_name="eureka"
            ;;
        "config-server")
            deployment_name="config-server-app"
            ;;
        "cloud-gateway")
            deployment_name="cloud-gateway-app"
            ;;
        "policy-service")
            deployment_name="policy-service-app"
            ;;
        "product-service")
            deployment_name="product-service-app"
            ;;
        "payment-service")
            deployment_name="payment-service-app"
            ;;
        "claim-service")
            deployment_name="claim-service-app"
            ;;
    esac
    
    if [[ -n "$deployment_name" ]]; then
        # Get recent logs and check for errors
        local logs=$(kubectl logs deployment/"$deployment_name" -n "$NAMESPACE" --tail=100 2>/dev/null || echo "")
        
        if [[ -n "$logs" ]]; then
            # Check for error patterns
            local error_count=$(echo "$logs" | grep -i "error\|exception\|failed" | wc -l)
            
            if [[ $error_count -gt 0 ]]; then
                log_warning "Found $error_count potential errors in $service_name logs"
                
                if [[ "$VERBOSE" == "true" ]]; then
                    echo "$logs" | grep -i "error\|exception\|failed" | tail -10
                fi
            else
                log_success "No errors found in $service_name logs"
            fi
        else
            log_warning "No logs available for $service_name"
        fi
    else
        log_warning "Unknown service: $service_name"
    fi
}

# Send Slack alert
send_slack_alert() {
    local message="$1"
    
    if [[ -n "$SLACK_WEBHOOK" ]]; then
        log_info "Sending Slack alert..."
        
        local payload=$(cat << EOF
{
    "text": "🚨 Insurance Project Monitoring Alert",
    "attachments": [
        {
            "color": "danger",
            "text": "$message",
            "fields": [
                {
                    "title": "Namespace",
                    "value": "$NAMESPACE",
                    "short": true
                },
                {
                    "title": "Timestamp",
                    "value": "$(date -u +"%Y-%m-%d %H:%M:%S UTC")",
                    "short": true
                }
            ]
        }
    ]
}
EOF
)
        
        if curl -X POST -H 'Content-type: application/json' --data "$payload" "$SLACK_WEBHOOK" > /dev/null 2>&1; then
            log_success "Slack alert sent successfully"
        else
            log_error "Failed to send Slack alert"
        fi
    fi
}

# Run health checks
run_health_checks() {
    log_info "Running health checks..."
    
    local failed_services=()
    local port_forward_pid=""
    
    # Get service URLs
    port_forward_pid=$(get_service_urls)
    
    # Check each service
    for service in "${!SERVICE_ENDPOINTS[@]}"; do
        if ! check_service_health "$service" "${SERVICE_ENDPOINTS[$service]}" "$port_forward_pid"; then
            failed_services+=("$service")
        fi
    done
    
    # Cleanup port-forward if used
    if [[ -n "$port_forward_pid" ]]; then
        kill "$port_forward_pid" 2>/dev/null || true
    fi
    
    if [[ ${#failed_services[@]} -gt 0 ]]; then
        local message="Health checks failed for services: ${failed_services[*]}"
        log_error "$message"
        
        if [[ "$ALERT_ON_FAILURE" == "true" ]]; then
            send_slack_alert "$message"
        fi
        
        return 1
    fi
    
    log_success "All health checks passed"
}

# Run metrics checks
run_metrics_checks() {
    log_info "Running metrics checks..."
    
    local failed_services=()
    
    for service in "${!SERVICE_ENDPOINTS[@]}"; do
        if ! check_service_metrics "$service" "${SERVICE_ENDPOINTS[$service]}"; then
            failed_services+=("$service")
        fi
    done
    
    if [[ ${#failed_services[@]} -gt 0 ]]; then
        local message="Metrics checks failed for services: ${failed_services[*]}"
        log_error "$message"
        
        if [[ "$ALERT_ON_FAILURE" == "true" ]]; then
            send_slack_alert "$message"
        fi
        
        return 1
    fi
    
    log_success "All metrics checks passed"
}

# Run log checks
run_log_checks() {
    log_info "Running log checks..."
    
    local failed_services=()
    
    for service in "${!SERVICE_ENDPOINTS[@]}"; do
        if ! check_service_logs "$service"; then
            failed_services+=("$service")
        fi
    done
    
    if [[ ${#failed_services[@]} -gt 0 ]]; then
        local message="Log checks failed for services: ${failed_services[*]}"
        log_error "$message"
        
        if [[ "$ALERT_ON_FAILURE" == "true" ]]; then
            send_slack_alert "$message"
        fi
        
        return 1
    fi
    
    log_success "All log checks passed"
}

# Main execution
main() {
    log_info "Starting monitoring process"
    log_info "Namespace: $NAMESPACE"
    log_info "Check type: $CHECK_TYPE"
    log_info "Timeout: $TIMEOUT seconds"
    log_info "Retries: $RETRIES"
    
    if [[ "$VERBOSE" == "true" ]]; then
        log_info "Verbose mode enabled"
    fi
    
    if [[ "$ALERT_ON_FAILURE" == "true" ]]; then
        log_info "Alerts enabled"
    fi
    
    # Check prerequisites
    check_prerequisites
    
    # Execute checks based on type
    case $CHECK_TYPE in
        "health")
            run_health_checks
            ;;
        "metrics")
            run_metrics_checks
            ;;
        "logs")
            run_log_checks
            ;;
        "all")
            run_health_checks
            run_metrics_checks
            run_log_checks
            ;;
    esac
    
    log_success "Monitoring process completed successfully!"
}

# Run main function
main "$@" 