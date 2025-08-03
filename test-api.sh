#!/bin/bash

# PMES Energy Planning System - API Test Script
# This script tests all major API endpoints

echo "🧪 PMES Energy Planning System - API Test Suite"
echo "=============================================="

# Configuration
BASE_URL="http://localhost:8080"
API_BASE="$BASE_URL/api/energy"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print test results
print_result() {
    local test_name="$1"
    local status="$2"
    local message="$3"
    
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✅ PASS${NC} - $test_name: $message"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC} - $test_name: $message"
        ((TESTS_FAILED++))
    fi
}

# Function to check if service is running
check_service() {
    echo -e "${BLUE}🔍 Checking if PMES service is running...${NC}"
    
    if curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo -e "${GREEN}✅ Service is running${NC}"
        return 0
    else
        echo -e "${RED}❌ Service is not running. Please start the application first.${NC}"
        echo "Run: ./start.sh or docker-compose up -d"
        exit 1
    fi
}

# Function to test health endpoint
test_health() {
    echo -e "\n${BLUE}🏥 Testing Health Endpoint${NC}"
    
    local response=$(curl -s -w "%{http_code}" "$BASE_URL/actuator/health")
    local status_code="${response: -3}"
    local body="${response%???}"
    
    if [ "$status_code" = "200" ]; then
        print_result "Health Check" "PASS" "Service is healthy"
    else
        print_result "Health Check" "FAIL" "Status code: $status_code"
    fi
}

# Function to test Swagger documentation
test_swagger() {
    echo -e "\n${BLUE}📚 Testing Swagger Documentation${NC}"
    
    local response=$(curl -s -w "%{http_code}" "$BASE_URL/swagger-ui.html")
    local status_code="${response: -3}"
    
    if [ "$status_code" = "200" ]; then
        print_result "Swagger UI" "PASS" "Documentation accessible"
    else
        print_result "Swagger UI" "FAIL" "Status code: $status_code"
    fi
}

# Function to test scenario creation
test_scenario_creation() {
    echo -e "\n${BLUE}📊 Testing Scenario Creation${NC}"
    
    local scenario_data='{
        "name": "Test Scenario",
        "description": "Test scenario for API testing",
        "startYear": 2023,
        "endYear": 2050,
        "sectorGrowthRates": {
            "Industrial": 0.02,
            "Residential": 0.015
        },
        "energySourceAdjustments": {
            "Renewables": 0.1,
            "Coal": -0.05
        }
    }'
    
    local response=$(curl -s -w "%{http_code}" -X POST "$API_BASE/scenarios" \
        -H "Content-Type: application/json" \
        -d "$scenario_data")
    
    local status_code="${response: -3}"
    local body="${response%???}"
    
    if [ "$status_code" = "201" ]; then
        # Extract scenario ID from response
        local scenario_id=$(echo "$body" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        echo "$scenario_id" > /tmp/scenario_id.txt
        print_result "Scenario Creation" "PASS" "Scenario created with ID: $scenario_id"
    else
        print_result "Scenario Creation" "FAIL" "Status code: $status_code"
    fi
}

# Function to test scenario retrieval
test_scenario_retrieval() {
    echo -e "\n${BLUE}📋 Testing Scenario Retrieval${NC}"
    
    if [ -f /tmp/scenario_id.txt ]; then
        local scenario_id=$(cat /tmp/scenario_id.txt)
        local response=$(curl -s -w "%{http_code}" "$API_BASE/scenarios/$scenario_id")
        local status_code="${response: -3}"
        
        if [ "$status_code" = "200" ]; then
            print_result "Scenario Retrieval" "PASS" "Scenario retrieved successfully"
        else
            print_result "Scenario Retrieval" "FAIL" "Status code: $status_code"
        fi
    else
        print_result "Scenario Retrieval" "FAIL" "No scenario ID available"
    fi
}

# Function to test baseline forecasting
test_baseline_forecast() {
    echo -e "\n${BLUE}🔮 Testing Baseline Forecasting${NC}"
    
    local forecast_data='{
        "sectors": ["Industrial", "Residential"],
        "energySources": ["Electricity", "Natural Gas"],
        "forecastYears": 5,
        "startYear": 2023,
        "endYear": 2028
    }'
    
    local response=$(curl -s -w "%{http_code}" -X POST "$API_BASE/forecast/baseline" \
        -H "Content-Type: application/json" \
        -d "$forecast_data")
    
    local status_code="${response: -3}"
    
    if [ "$status_code" = "200" ]; then
        print_result "Baseline Forecasting" "PASS" "Forecast completed successfully"
    else
        print_result "Baseline Forecasting" "FAIL" "Status code: $status_code"
    fi
}

# Function to test scenario-based forecasting
test_scenario_forecast() {
    echo -e "\n${BLUE}📈 Testing Scenario-based Forecasting${NC}"
    
    if [ -f /tmp/scenario_id.txt ]; then
        local scenario_id=$(cat /tmp/scenario_id.txt)
        local forecast_data='{
            "sectors": ["Industrial"],
            "energySources": ["Electricity"],
            "forecastYears": 3
        }'
        
        local response=$(curl -s -w "%{http_code}" -X POST "$API_BASE/scenarios/$scenario_id/forecast" \
            -H "Content-Type: application/json" \
            -d "$forecast_data")
        
        local status_code="${response: -3}"
        
        if [ "$status_code" = "200" ]; then
            print_result "Scenario Forecasting" "PASS" "Scenario forecast completed"
        else
            print_result "Scenario Forecasting" "FAIL" "Status code: $status_code"
        fi
    else
        print_result "Scenario Forecasting" "FAIL" "No scenario ID available"
    fi
}

# Function to test emissions calculation
test_emissions_calculation() {
    echo -e "\n${BLUE}🌍 Testing Emissions Calculation${NC}"
    
    if [ -f /tmp/scenario_id.txt ]; then
        local scenario_id=$(cat /tmp/scenario_id.txt)
        local response=$(curl -s -w "%{http_code}" "$API_BASE/scenarios/$scenario_id/emissions")
        local status_code="${response: -3}"
        
        if [ "$status_code" = "200" ]; then
            print_result "Emissions Calculation" "PASS" "Emissions calculated successfully"
        else
            print_result "Emissions Calculation" "FAIL" "Status code: $status_code"
        fi
    else
        print_result "Emissions Calculation" "FAIL" "No scenario ID available"
    fi
}

# Function to test Excel import (if sample data exists)
test_excel_import() {
    echo -e "\n${BLUE}📊 Testing Excel Import${NC}"
    
    if [ -f "sample-data/energy_data_sample.csv" ]; then
        local response=$(curl -s -w "%{http_code}" -X POST "$API_BASE/import/excel" \
            -F "file=@sample-data/energy_data_sample.csv" \
            -F "dataSource=Test Import")
        
        local status_code="${response: -3}"
        
        if [ "$status_code" = "200" ]; then
            print_result "Excel Import" "PASS" "Data imported successfully"
        else
            print_result "Excel Import" "FAIL" "Status code: $status_code"
        fi
    else
        print_result "Excel Import" "SKIP" "No sample data file found"
    fi
}

# Function to test scenario comparison
test_scenario_comparison() {
    echo -e "\n${BLUE}⚖️ Testing Scenario Comparison${NC}"
    
    if [ -f /tmp/scenario_id.txt ]; then
        local scenario_id=$(cat /tmp/scenario_id.txt)
        local response=$(curl -s -w "%{http_code}" "$API_BASE/emissions/compare?scenario1Id=$scenario_id&scenario2Id=$scenario_id")
        local status_code="${response: -3}"
        
        if [ "$status_code" = "200" ]; then
            print_result "Scenario Comparison" "PASS" "Comparison completed successfully"
        else
            print_result "Scenario Comparison" "FAIL" "Status code: $status_code"
        fi
    else
        print_result "Scenario Comparison" "FAIL" "No scenario ID available"
    fi
}

# Function to test combined forecast with emissions
test_combined_forecast() {
    echo -e "\n${BLUE}🔄 Testing Combined Forecast with Emissions${NC}"
    
    if [ -f /tmp/scenario_id.txt ]; then
        local scenario_id=$(cat /tmp/scenario_id.txt)
        local forecast_data='{
            "sectors": ["Industrial"],
            "energySources": ["Electricity"],
            "forecastYears": 2
        }'
        
        local response=$(curl -s -w "%{http_code}" -X POST "$API_BASE/scenarios/$scenario_id/forecast-with-emissions" \
            -H "Content-Type: application/json" \
            -d "$forecast_data")
        
        local status_code="${response: -3}"
        
        if [ "$status_code" = "200" ]; then
            print_result "Combined Forecast" "PASS" "Combined forecast completed"
        else
            print_result "Combined Forecast" "FAIL" "Status code: $status_code"
        fi
    else
        print_result "Combined Forecast" "FAIL" "No scenario ID available"
    fi
}

# Function to test error handling
test_error_handling() {
    echo -e "\n${BLUE}🚨 Testing Error Handling${NC}"
    
    # Test invalid scenario ID
    local response=$(curl -s -w "%{http_code}" "$API_BASE/scenarios/99999")
    local status_code="${response: -3}"
    
    if [ "$status_code" = "404" ]; then
        print_result "Error Handling" "PASS" "Proper 404 response for invalid ID"
    else
        print_result "Error Handling" "FAIL" "Expected 404, got $status_code"
    fi
}

# Function to print summary
print_summary() {
    echo -e "\n${BLUE}📊 Test Summary${NC}"
    echo "=================="
    echo -e "${GREEN}✅ Tests Passed: $TESTS_PASSED${NC}"
    echo -e "${RED}❌ Tests Failed: $TESTS_FAILED${NC}"
    
    local total=$((TESTS_PASSED + TESTS_FAILED))
    if [ $total -gt 0 ]; then
        local success_rate=$((TESTS_PASSED * 100 / total))
        echo -e "${BLUE}📈 Success Rate: $success_rate%${NC}"
    fi
    
    echo -e "\n${YELLOW}🔗 Useful Links:${NC}"
    echo "• Application: $BASE_URL"
    echo "• Swagger UI: $BASE_URL/swagger-ui.html"
    echo "• Health Check: $BASE_URL/actuator/health"
    echo "• API Docs: $BASE_URL/api-docs"
}

# Function to cleanup
cleanup() {
    if [ -f /tmp/scenario_id.txt ]; then
        rm /tmp/scenario_id.txt
    fi
}

# Main execution
main() {
    echo -e "${YELLOW}Starting PMES API Test Suite...${NC}"
    
    # Check if service is running
    check_service
    
    # Run all tests
    test_health
    test_swagger
    test_scenario_creation
    test_scenario_retrieval
    test_baseline_forecast
    test_scenario_forecast
    test_emissions_calculation
    test_excel_import
    test_scenario_comparison
    test_combined_forecast
    test_error_handling
    
    # Print summary
    print_summary
    
    # Cleanup
    cleanup
    
    echo -e "\n${GREEN}🎉 Test suite completed!${NC}"
}

# Run main function
main "$@" 