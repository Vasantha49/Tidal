#!/bin/bash
# Production deployment script for Music Service

echo "Starting production deployment..."

# Build the application
echo "Building Docker image..."
docker-compose -f docker-compose.prod.yml build

# Start the services
echo "Starting services..."
docker-compose -f docker-compose.prod.yml up -d

# Wait for services to be healthy
echo "Waiting for services to be healthy..."
sleep 30

# Check service status
echo "Checking service status..."
docker-compose -f docker-compose.prod.yml ps

# Show logs
echo "Showing recent logs..."
docker-compose -f docker-compose.prod.yml logs --tail=20 app

echo ""
echo "Deployment completed!"
echo "Application should be available at: http://localhost:8080"
echo "Health check endpoint: http://localhost:8080/health/tidal"
echo ""
echo "To stop the services, run: docker-compose -f docker-compose.prod.yml down"
echo "To view logs: docker-compose -f docker-compose.prod.yml logs -f app"
