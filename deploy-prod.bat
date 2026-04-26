@echo off
REM Production deployment script for Music Service

echo Starting production deployment...

REM Build the application
echo Building Docker image...
docker-compose -f docker-compose.prod.yml build

REM Start the services
echo Starting services...
docker-compose -f docker-compose.prod.yml up -d

REM Wait for services to be healthy
echo Waiting for services to be healthy...
timeout /t 30 /nobreak > nul

REM Check service status
echo Checking service status...
docker-compose -f docker-compose.prod.yml ps

REM Show logs
echo Showing recent logs...
docker-compose -f docker-compose.prod.yml logs --tail=20 app

echo.
echo Deployment completed!
echo Application should be available at: http://localhost:8080
echo Health check endpoint: http://localhost:8080/health/tidal
echo.
echo To stop the services, run: docker-compose -f docker-compose.prod.yml down
echo To view logs: docker-compose -f docker-compose.prod.yml logs -f app
