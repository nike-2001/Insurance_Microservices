#!/bin/bash

# Insurance Project Test Script
# This script handles running tests for all microservices

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Default values
TEST_TYPE="all"
SERVICE=""
INTEGRATION_TESTS=false
PERFORMANCE_TESTS=false
COVERAGE_REPORT=false
PARALLEL=false
VERBOSE=false

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
Insurance Project Test Script

Usage: $0 [OPTIONS]

Options:
    -t, --type TYPE          Test type (all|unit|integration|performance) [default: all]
    -s, --service SERVICE    Specific service to test
    -i, --integration        Run integration tests
    -p, --performance        Run performance tests
    -c, --coverage           Generate coverage report
    --parallel               Run tests in parallel
    -v, --verbose            Verbose output
    -h, --help               Show this help message

Services:
    serviceregistry, configserver, cloudgateway, policyservice, 
    productservice, paymentservice, claimservice

Examples:
    $0 -t all -c
    $0 -t unit -s policyservice
    $0 -t integration --parallel
    $0 -t performance -v

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--type)
            TEST_TYPE="$2"
            shift 2
            ;;
        -s|--service)
            SERVICE="$2"
            shift 2
            ;;
        -i|--integration)
            INTEGRATION_TESTS=true
            shift
            ;;
        -p|--performance)
            PERFORMANCE_TESTS=true
            shift
            ;;
        -c|--coverage)
            COVERAGE_REPORT=true
            shift
            ;;
        --parallel)
            PARALLEL=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
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

# Validate test type
case $TEST_TYPE in
    all|unit|integration|performance)
        ;;
    *)
        log_error "Invalid test type: $TEST_TYPE. Must be all, unit, integration, or performance"
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
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed"
        exit 1
    fi
    
    # Check if curl is installed (for integration tests)
    if [[ "$INTEGRATION_TESTS" == "true" ]] && ! command -v curl &> /dev/null; then
        log_error "curl is not installed (required for integration tests)"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Run unit tests for a service
run_unit_tests() {
    local service_name="$1"
    local service_dir="$PROJECT_ROOT/$service_name"
    
    if [[ ! -d "$service_dir" ]]; then
        log_error "Service directory not found: $service_dir"
        return 1
    fi
    
    log_info "Running unit tests for: $service_name"
    
    cd "$service_dir"
    
    local mvn_args="test"
    
    if [[ "$COVERAGE_REPORT" == "true" ]]; then
        mvn_args="test jacoco:report"
    fi
    
    if [[ "$VERBOSE" == "true" ]]; then
        mvn_args="$mvn_args -X"
    fi
    
    if mvn $mvn_args; then
        log_success "Unit tests passed for $service_name"
        
        # Check coverage if enabled
        if [[ "$COVERAGE_REPORT" == "true" ]]; then
            local coverage_file="$service_dir/target/site/jacoco/index.html"
            if [[ -f "$coverage_file" ]]; then
                log_info "Coverage report generated: $coverage_file"
            fi
        fi
    else
        log_error "Unit tests failed for $service_name"
        return 1
    fi
    
    cd "$PROJECT_ROOT"
}

# Run integration tests for a service
run_integration_tests() {
    local service_name="$1"
    local service_dir="$PROJECT_ROOT/$service_name"
    
    if [[ ! -d "$service_dir" ]]; then
        log_error "Service directory not found: $service_dir"
        return 1
    fi
    
    log_info "Running integration tests for: $service_name"
    
    cd "$service_dir"
    
    # Check if integration test profile exists
    if mvn help:all-profiles | grep -q "integration"; then
        local mvn_args="test -Pintegration"
        
        if [[ "$VERBOSE" == "true" ]]; then
            mvn_args="$mvn_args -X"
        fi
        
        if mvn $mvn_args; then
            log_success "Integration tests passed for $service_name"
        else
            log_error "Integration tests failed for $service_name"
            return 1
        fi
    else
        log_warning "No integration test profile found for $service_name"
    fi
    
    cd "$PROJECT_ROOT"
}

# Run performance tests for a service
run_performance_tests() {
    local service_name="$1"
    local service_dir="$PROJECT_ROOT/$service_name"
    
    if [[ ! -d "$service_dir" ]]; then
        log_error "Service directory not found: $service_dir"
        return 1
    fi
    
    log_info "Running performance tests for: $service_name"
    
    cd "$service_dir"
    
    # Check if performance test profile exists
    if mvn help:all-profiles | grep -q "performance"; then
        local mvn_args="test -Pperformance"
        
        if [[ "$VERBOSE" == "true" ]]; then
            mvn_args="$mvn_args -X"
        fi
        
        if mvn $mvn_args; then
            log_success "Performance tests passed for $service_name"
        else
            log_error "Performance tests failed for $service_name"
            return 1
        fi
    else
        log_warning "No performance test profile found for $service_name"
    fi
    
    cd "$PROJECT_ROOT"
}

# Run all tests for a service
run_all_tests() {
    local service_name="$1"
    
    log_info "Running all tests for: $service_name"
    
    # Run unit tests
    if ! run_unit_tests "$service_name"; then
        return 1
    fi
    
    # Run integration tests if requested
    if [[ "$INTEGRATION_TESTS" == "true" ]]; then
        if ! run_integration_tests "$service_name"; then
            return 1
        fi
    fi
    
    # Run performance tests if requested
    if [[ "$PERFORMANCE_TESTS" == "true" ]]; then
        if ! run_performance_tests "$service_name"; then
            return 1
        fi
    fi
    
    log_success "All tests passed for $service_name"
}

# Run tests for all services
run_all_services_tests() {
    log_info "Running tests for all services..."
    
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
    
    if [[ "$PARALLEL" == "true" ]]; then
        log_info "Running tests in parallel..."
        
        # Run tests in parallel using background processes
        local pids=()
        for service in "${services[@]}"; do
            run_all_tests "$service" &
            pids+=($!)
        done
        
        # Wait for all background processes
        for pid in "${pids[@]}"; do
            if ! wait "$pid"; then
                failed_services+=("$service")
            fi
        done
    else
        # Run tests sequentially
        for service in "${services[@]}"; do
            if ! run_all_tests "$service"; then
                failed_services+=("$service")
            fi
        done
    fi
    
    if [[ ${#failed_services[@]} -gt 0 ]]; then
        log_error "Tests failed for services: ${failed_services[*]}"
        return 1
    fi
    
    log_success "All tests passed for all services"
}

# Run end-to-end integration tests
run_e2e_tests() {
    log_info "Running end-to-end integration tests..."
    
    # This would typically involve:
    # 1. Starting all services
    # 2. Running API tests
    # 3. Testing service interactions
    # 4. Validating business workflows
    
    log_warning "E2E tests not implemented yet"
    log_info "Consider using tools like TestContainers or WireMock for E2E testing"
}

# Generate aggregated coverage report
generate_coverage_report() {
    if [[ "$COVERAGE_REPORT" == "false" ]]; then
        return
    fi
    
    log_info "Generating aggregated coverage report..."
    
    # This would typically involve:
    # 1. Collecting coverage reports from all services
    # 2. Aggregating them into a single report
    # 3. Publishing to a coverage dashboard
    
    log_warning "Aggregated coverage report not implemented yet"
    log_info "Consider using tools like SonarQube or Codecov for coverage aggregation"
}

# Main execution
main() {
    log_info "Starting test process"
    log_info "Test type: $TEST_TYPE"
    
    if [[ "$INTEGRATION_TESTS" == "true" ]]; then
        log_info "Integration tests enabled"
    fi
    
    if [[ "$PERFORMANCE_TESTS" == "true" ]]; then
        log_info "Performance tests enabled"
    fi
    
    if [[ "$COVERAGE_REPORT" == "true" ]]; then
        log_info "Coverage reporting enabled"
    fi
    
    if [[ "$PARALLEL" == "true" ]]; then
        log_info "Parallel execution enabled"
    fi
    
    # Check prerequisites
    check_prerequisites
    
    # Execute tests based on type and service
    if [[ -n "$SERVICE" ]]; then
        case $TEST_TYPE in
            "all")
                run_all_tests "$SERVICE"
                ;;
            "unit")
                run_unit_tests "$SERVICE"
                ;;
            "integration")
                run_integration_tests "$SERVICE"
                ;;
            "performance")
                run_performance_tests "$SERVICE"
                ;;
        esac
    else
        case $TEST_TYPE in
            "all")
                run_all_services_tests
                run_e2e_tests
                ;;
            "unit")
                run_all_services_tests
                ;;
            "integration")
                run_e2e_tests
                ;;
            "performance")
                run_all_services_tests
                ;;
        esac
    fi
    
    # Generate coverage report
    generate_coverage_report
    
    log_success "Test process completed successfully!"
}

# Run main function
main "$@" 