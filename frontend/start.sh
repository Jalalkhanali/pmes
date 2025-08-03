#!/bin/bash

echo "🚀 Starting PMES Frontend - Next.js Energy Planning Interface"
echo "============================================================"

# Check if Node.js is available
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18+."
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Node.js 18+ is required. Current version: $(node -v)"
    exit 1
fi

echo "✅ Node.js version: $(node -v)"

# Check if npm is available
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm."
    exit 1
fi

echo "✅ npm version: $(npm -v)"

# Check if backend is running
echo "🔍 Checking backend connection..."
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ Backend is running on http://localhost:8080"
else
    echo "⚠️  Warning: Backend is not running on http://localhost:8080"
    echo "   Please start the backend first with: ./start.sh (from root directory)"
    echo "   Or with Docker: docker-compose up -d"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ Failed to install dependencies"
        exit 1
    fi
    echo "✅ Dependencies installed successfully"
else
    echo "✅ Dependencies already installed"
fi

# Check for TypeScript errors
echo "🔍 Checking TypeScript..."
npm run type-check
if [ $? -ne 0 ]; then
    echo "❌ TypeScript errors found. Please fix them before starting."
    exit 1
fi
echo "✅ TypeScript check passed"

# Create logs directory
mkdir -p logs

echo "🌐 Starting development server..."
echo "📱 Frontend will be available at: http://localhost:3000"
echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
echo "🏥 Health Check: http://localhost:8080/actuator/health"
echo ""
echo "Press Ctrl+C to stop the development server"
echo ""

# Start the development server
npm run dev 