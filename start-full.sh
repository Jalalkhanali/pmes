#!/bin/bash

echo "🚀 PMES Energy Planning System - Full Stack Startup"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    local service="$1"
    local status="$2"
    local message="$3"
    
    if [ "$status" = "OK" ]; then
        echo -e "${GREEN}✅${NC} $service: $message"
    elif [ "$status" = "WARN" ]; then
        echo -e "${YELLOW}⚠️${NC} $service: $message"
    else
        echo -e "${RED}❌${NC} $service: $message"
    fi
}

# Function to check if port is in use
check_port() {
    local port="$1"
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        return 0
    else
        return 1
    fi
}

# Function to wait for service
wait_for_service() {
    local url="$1"
    local service="$2"
    local max_attempts=30
    local attempt=1
    
    echo -n "Waiting for $service to be ready..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e " ${GREEN}OK${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    echo -e " ${RED}FAILED${NC}"
    return 1
}

# Check prerequisites
echo "🔍 Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        print_status "Java" "OK" "Version $(java -version 2>&1 | head -n 1)"
    else
        print_status "Java" "FAIL" "Java 21+ required, found version $JAVA_VERSION"
        exit 1
    fi
else
    print_status "Java" "FAIL" "Java not found"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    print_status "Maven" "OK" "Version $(mvn -version | head -n 1)"
else
    print_status "Maven" "FAIL" "Maven not found"
    exit 1
fi

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$NODE_VERSION" -ge 18 ]; then
        print_status "Node.js" "OK" "Version $(node -v)"
    else
        print_status "Node.js" "FAIL" "Node.js 18+ required, found version $NODE_VERSION"
        exit 1
    fi
else
    print_status "Node.js" "FAIL" "Node.js not found"
    exit 1
fi

# Check npm
if command -v npm &> /dev/null; then
    print_status "npm" "OK" "Version $(npm -v)"
else
    print_status "npm" "FAIL" "npm not found"
    exit 1
fi

# Check Docker (optional)
if command -v docker &> /dev/null; then
    print_status "Docker" "OK" "Version $(docker --version)"
    DOCKER_AVAILABLE=true
else
    print_status "Docker" "WARN" "Docker not found - will use local development"
    DOCKER_AVAILABLE=false
fi

# Check Docker Compose (optional)
if command -v docker-compose &> /dev/null; then
    print_status "Docker Compose" "OK" "Available"
    COMPOSE_AVAILABLE=true
else
    print_status "Docker Compose" "WARN" "Docker Compose not found"
    COMPOSE_AVAILABLE=false
fi

echo ""
echo "📋 Startup Options:"
echo "1. Full Stack (Backend + Frontend + Database)"
echo "2. Backend Only"
echo "3. Frontend Only"
echo "4. Docker (Full Stack)"
echo "5. Docker (Backend Only)"
echo ""

read -p "Select option (1-5): " choice

case $choice in
    1)
        echo "🚀 Starting Full Stack (Local Development)..."
        start_full_stack_local
        ;;
    2)
        echo "🚀 Starting Backend Only..."
        start_backend_only
        ;;
    3)
        echo "🚀 Starting Frontend Only..."
        start_frontend_only
        ;;
    4)
        if [ "$DOCKER_AVAILABLE" = true ] && [ "$COMPOSE_AVAILABLE" = true ]; then
            echo "🚀 Starting Full Stack (Docker)..."
            start_full_stack_docker
        else
            echo "❌ Docker or Docker Compose not available"
            exit 1
        fi
        ;;
    5)
        if [ "$DOCKER_AVAILABLE" = true ] && [ "$COMPOSE_AVAILABLE" = true ]; then
            echo "🚀 Starting Backend (Docker)..."
            start_backend_docker
        else
            echo "❌ Docker or Docker Compose not available"
            exit 1
        fi
        ;;
    *)
        echo "❌ Invalid option"
        exit 1
        ;;
esac

# Function: Start full stack locally
start_full_stack_local() {
    echo ""
    echo "🔧 Setting up full stack..."
    
    # Check if ports are available
    if check_port 8080; then
        print_status "Port 8080" "FAIL" "Port 8080 is already in use"
        exit 1
    fi
    
    if check_port 3000; then
        print_status "Port 3000" "FAIL" "Port 3000 is already in use"
        exit 1
    fi
    
    if check_port 5432; then
        print_status "Port 5432" "WARN" "Port 5432 is in use - PostgreSQL might be running"
    fi
    
    # Start PostgreSQL (if not running)
    if ! check_port 5432; then
        echo "🐘 Starting PostgreSQL..."
        if command -v pg_ctl &> /dev/null; then
            # Try to start PostgreSQL service
            sudo systemctl start postgresql 2>/dev/null || true
        else
            echo "⚠️  PostgreSQL not found. Please install and start PostgreSQL manually."
            echo "   Or use Docker: docker run --name pmes-postgres -e POSTGRES_DB=energy_planning -e POSTGRES_USER=energy_user -e POSTGRES_PASSWORD=energy_pass -p 5432:5432 -d postgres:15-alpine"
        fi
    fi
    
    # Start Backend
    echo "🔧 Starting Backend..."
    cd "$(dirname "$0")"
    ./start.sh &
    BACKEND_PID=$!
    
    # Wait for backend
    if wait_for_service "http://localhost:8080/actuator/health" "Backend"; then
        print_status "Backend" "OK" "Running on http://localhost:8080"
    else
        print_status "Backend" "FAIL" "Failed to start"
        kill $BACKEND_PID 2>/dev/null
        exit 1
    fi
    
    # Start Frontend
    echo "🌐 Starting Frontend..."
    cd frontend
    if [ ! -d "node_modules" ]; then
        echo "📦 Installing frontend dependencies..."
        npm install
    fi
    
    npm run dev &
    FRONTEND_PID=$!
    
    # Wait for frontend
    if wait_for_service "http://localhost:3000" "Frontend"; then
        print_status "Frontend" "OK" "Running on http://localhost:3000"
    else
        print_status "Frontend" "FAIL" "Failed to start"
        kill $FRONTEND_PID 2>/dev/null
        kill $BACKEND_PID 2>/dev/null
        exit 1
    fi
    
    echo ""
    echo "🎉 PMES Energy Planning System is running!"
    echo ""
    echo "📱 Frontend: http://localhost:3000"
    echo "🔧 Backend API: http://localhost:8080"
    echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
    echo "🏥 Health Check: http://localhost:8080/actuator/health"
    echo ""
    echo "Press Ctrl+C to stop all services"
    
    # Wait for interrupt
    trap 'echo ""; echo "🛑 Stopping services..."; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0' INT
    wait
}

# Function: Start backend only
start_backend_only() {
    echo ""
    echo "🔧 Starting Backend..."
    cd "$(dirname "$0")"
    ./start.sh
}

# Function: Start frontend only
start_frontend_only() {
    echo ""
    echo "🌐 Starting Frontend..."
    cd frontend
    if [ ! -d "node_modules" ]; then
        echo "📦 Installing frontend dependencies..."
        npm install
    fi
    npm run dev
}

# Function: Start full stack with Docker
start_full_stack_docker() {
    echo ""
    echo "🐳 Starting Full Stack with Docker..."
    cd "$(dirname "$0")"
    
    if [ -f "docker-compose.full.yml" ]; then
        docker-compose -f docker-compose.full.yml up -d
        echo ""
        echo "⏳ Waiting for services to start..."
        sleep 10
        
        if wait_for_service "http://localhost:3000" "Frontend"; then
            print_status "Frontend" "OK" "Running on http://localhost:3000"
        else
            print_status "Frontend" "FAIL" "Failed to start"
        fi
        
        if wait_for_service "http://localhost:8080/actuator/health" "Backend"; then
            print_status "Backend" "OK" "Running on http://localhost:8080"
        else
            print_status "Backend" "FAIL" "Failed to start"
        fi
        
        echo ""
        echo "🎉 PMES Energy Planning System is running!"
        echo ""
        echo "📱 Frontend: http://localhost:3000"
        echo "🔧 Backend API: http://localhost:8080"
        echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
        echo ""
        echo "To stop: docker-compose -f docker-compose.full.yml down"
    else
        echo "❌ docker-compose.full.yml not found"
        exit 1
    fi
}

# Function: Start backend with Docker
start_backend_docker() {
    echo ""
    echo "🐳 Starting Backend with Docker..."
    cd "$(dirname "$0")"
    
    if [ -f "compose.yaml" ]; then
        docker-compose up -d
        echo ""
        echo "⏳ Waiting for backend to start..."
        sleep 10
        
        if wait_for_service "http://localhost:8080/actuator/health" "Backend"; then
            print_status "Backend" "OK" "Running on http://localhost:8080"
            echo ""
            echo "🎉 PMES Backend is running!"
            echo ""
            echo "🔧 Backend API: http://localhost:8080"
            echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
            echo ""
            echo "To start frontend: cd frontend && npm run dev"
            echo "To stop: docker-compose down"
        else
            print_status "Backend" "FAIL" "Failed to start"
        fi
    else
        echo "❌ compose.yaml not found"
        exit 1
    fi
} 