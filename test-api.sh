#!/bin/bash

# Test script for PMES Energy Planning API
# Make sure the application is running on http://localhost:8080

BASE_URL="http://localhost:8080"

echo "🧪 Testing PMES Energy Planning API"
echo "=================================="

# Test health endpoint
echo "1. Testing health endpoint..."
curl -s -X GET "$BASE_URL/actuator/health" | jq '.' || echo "Health check failed"

# Test scenarios endpoint
echo -e "\n2. Testing scenarios endpoint..."
curl -s -X GET "$BASE_URL/api/energy/scenarios" | jq '.' || echo "Scenarios endpoint failed"

# Test emission factors endpoint
echo -e "\n3. Testing emission factors endpoint..."
curl -s -X GET "$BASE_URL/api/energy/data/emission-factors" | jq '.' || echo "Emission factors endpoint failed"

# Test energy data endpoint
echo -e "\n4. Testing energy data endpoint..."
curl -s -X GET "$BASE_URL/api/energy/data/energy" | jq '.' || echo "Energy data endpoint failed"

# Test baseline forecast (if data exists)
echo -e "\n5. Testing baseline forecast..."
curl -s -X POST "$BASE_URL/api/energy/forecast/baseline" \
  -H "Content-Type: application/json" \
  -d '{
    "sectors": ["INDUSTRIAL", "RESIDENTIAL"],
    "energySources": ["COAL", "GAS", "RENEWABLES"],
    "forecastYears": 5,
    "startYear": 2020,
    "endYear": 2025
  }' | jq '.' || echo "Baseline forecast failed"

echo -e "\n✅ API testing completed!"
echo "📚 Swagger UI: $BASE_URL/swagger-ui.html"
echo "📊 Health Check: $BASE_URL/actuator/health" 