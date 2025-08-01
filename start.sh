#!/bin/bash

echo "🚀 Starting PMES Energy Planning System"
echo "======================================"

# Check if Java 21 is available
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java 21 is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.8+."
    exit 1
fi

echo "✅ Maven version: $(mvn -version | head -n 1)"

# Create logs directory
mkdir -p logs

echo "🔧 Building application..."
mvn clean compile -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful"
    echo "🌐 Starting application on http://localhost:8080"
    echo "📚 Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "📊 Health Check: http://localhost:8080/actuator/health"
    echo ""
    echo "Press Ctrl+C to stop the application"
    echo ""
    
    mvn spring-boot:run
else
    echo "❌ Build failed"
    exit 1
fi 