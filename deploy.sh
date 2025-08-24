#!/bin/bash

# JavaGIS BASF Enterprise Application Deployment Script
# Usage: ./deploy.sh [environment] [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="development"
BUILD_FRONTEND=true
BUILD_BACKEND=true
RUN_TESTS=false
CLEAN_BUILD=false

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

# Function to show usage
show_usage() {
    echo "Usage: $0 [environment] [options]"
    echo ""
    echo "Environments:"
    echo "  development  - Local development environment (default)"
    echo "  staging      - Staging environment"
    echo "  production   - Production environment"
    echo ""
    echo "Options:"
    echo "  --no-frontend    Skip frontend build"
    echo "  --no-backend     Skip backend build"
    echo "  --test           Run tests before deployment"
    echo "  --clean          Clean build (remove existing containers and volumes)"
    echo "  --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 development"
    echo "  $0 production --test --clean"
    echo "  $0 staging --no-frontend"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        development|staging|production)
            ENVIRONMENT="$1"
            shift
            ;;
        --no-frontend)
            BUILD_FRONTEND=false
            shift
            ;;
        --no-backend)
            BUILD_BACKEND=false
            shift
            ;;
        --test)
            RUN_TESTS=true
            shift
            ;;
        --clean)
            CLEAN_BUILD=true
            shift
            ;;
        --help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

print_status "Starting JavaGIS BASF deployment for environment: $ENVIRONMENT"

# Check prerequisites
print_status "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

print_success "Prerequisites check passed"

# Clean build if requested
if [ "$CLEAN_BUILD" = true ]; then
    print_status "Performing clean build..."
    docker-compose down -v --remove-orphans
    docker system prune -f
    print_success "Clean build completed"
fi

# Set environment variables based on environment
case $ENVIRONMENT in
    development)
        export COMPOSE_FILE="docker-compose.yml"
        export SPRING_PROFILES_ACTIVE="dev"
        ;;
    staging)
        export COMPOSE_FILE="docker-compose.yml:docker-compose.staging.yml"
        export SPRING_PROFILES_ACTIVE="staging"
        ;;
    production)
        export COMPOSE_FILE="docker-compose.yml:docker-compose.prod.yml"
        export SPRING_PROFILES_ACTIVE="prod"
        ;;
esac

# Run tests if requested
if [ "$RUN_TESTS" = true ]; then
    print_status "Running tests..."

    if [ "$BUILD_BACKEND" = true ]; then
        print_status "Running backend tests..."
        mvn test
        print_success "Backend tests passed"
    fi

    if [ "$BUILD_FRONTEND" = true ]; then
        print_status "Running frontend tests..."
        cd javagis-frontend
        pnpm test --run
        cd ..
        print_success "Frontend tests passed"
    fi
fi

# Build and deploy
print_status "Building and deploying application..."

# Build services
BUILD_ARGS=""
if [ "$BUILD_FRONTEND" = false ]; then
    BUILD_ARGS="$BUILD_ARGS --no-deps backend database redis"
elif [ "$BUILD_BACKEND" = false ]; then
    BUILD_ARGS="$BUILD_ARGS --no-deps frontend"
fi

# Start services
docker-compose up -d --build $BUILD_ARGS

# Wait for services to be healthy
print_status "Waiting for services to be healthy..."
sleep 30

# Check service health
print_status "Checking service health..."

# Check database
if docker-compose ps database | grep -q "healthy"; then
    print_success "Database is healthy"
else
    print_warning "Database health check failed"
fi

# Check backend
if [ "$BUILD_BACKEND" = true ]; then
    if curl -f http://localhost:8080/api/actuator/health &> /dev/null; then
        print_success "Backend is healthy"
    else
        print_warning "Backend health check failed"
    fi
fi

# Check frontend
if [ "$BUILD_FRONTEND" = true ]; then
    if curl -f http://localhost:80/health &> /dev/null; then
        print_success "Frontend is healthy"
    else
        print_warning "Frontend health check failed"
    fi
fi

# Display deployment information
print_success "Deployment completed successfully!"
echo ""
echo "=== Deployment Information ==="
echo "Environment: $ENVIRONMENT"
echo "Frontend URL: http://localhost:80"
echo "Backend API: http://localhost:8080/api"
echo "Database: localhost:5432"
echo ""
echo "=== Service Status ==="
docker-compose ps
echo ""
echo "=== Useful Commands ==="
echo "View logs: docker-compose logs -f [service_name]"
echo "Stop services: docker-compose down"
echo "Restart service: docker-compose restart [service_name]"
echo "Scale service: docker-compose up -d --scale [service_name]=3"
echo ""

# Environment-specific instructions
case $ENVIRONMENT in
    development)
        echo "=== Development Environment ==="
        echo "- Hot reload is enabled for frontend development"
        echo "- Database data is persisted in Docker volumes"
        echo "- Debug logging is enabled"
        ;;
    staging)
        echo "=== Staging Environment ==="
        echo "- SSL certificates should be configured"
        echo "- Monitor application logs for issues"
        echo "- Performance testing recommended"
        ;;
    production)
        echo "=== Production Environment ==="
        echo "- Ensure SSL certificates are valid"
        echo "- Monitor system resources and performance"
        echo "- Set up backup procedures for database"
        echo "- Configure log rotation and monitoring"
        ;;
esac

print_success "JavaGIS Enterprise Application is now running!"
