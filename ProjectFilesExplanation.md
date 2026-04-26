# Music Service Project - File Explanations

This document provides detailed explanations for each file in the Music Service project, a Spring Boot application that integrates with TIDAL's music API to manage artists and albums.

## Project Structure Overview

The project follows a standard Maven-based Spring Boot structure with the following main directories:
- `src/main/java`: Source code
- `src/main/resources`: Configuration and static resources
- `src/test/java`: Test code
- `target/`: Compiled classes and build artifacts

## Root Level Files

### Build and Dependency Management
- **`pom.xml`**: Maven Project Object Model file defining project dependencies, plugins, and build configuration. Includes Spring Boot starters for web, data JPA, security, and testing frameworks.
- **`mvnw`**: Maven wrapper script for Unix/Linux systems, allowing Maven execution without local installation.
- **`mvnw.cmd`**: Maven wrapper script for Windows systems.

### Docker Configuration
- **`Dockerfile`**: Multi-stage Docker build file for creating production-ready container images. Uses Maven for build stage and Eclipse Temurin JRE for runtime.
- **`docker-compose.yml`**: Development Docker Compose configuration with PostgreSQL database and application services.
- **`docker-compose.prod.yml`**: Production Docker Compose configuration with optimized settings, health checks, and environment variables.
- **`.dockerignore`**: Specifies files and directories to exclude from Docker build context for optimized image size.
- **`.env.example`**: Template file showing required environment variables for production deployment.

### Version Control and IDE
- **`.gitignore`**: Git ignore rules excluding build artifacts, IDE files, and sensitive data.
- **`.gitattributes`**: Git attributes for handling line endings and file types.
- **`.idea/`**: IntelliJ IDEA project configuration files (compiler.xml, encodings.xml, misc.xml, vcs.xml, workspace.xml, etc.).

### Documentation
- **`README.md`**: Main project documentation with setup instructions, API usage, and deployment guides.
- **`HELP.md`**: Additional help documentation and troubleshooting guides.

### Deployment Scripts
- **`deploy-prod.sh`**: Bash script for production deployment on Unix/Linux systems.
- **`deploy-prod.bat`**: Batch script for production deployment on Windows systems.

## Source Code (src/main/java/com/immomio/tidal/music/)

### Main Application
- **`MusicApplication.java`**: Spring Boot main application class with @SpringBootApplication annotation, serving as the application entry point.

### Aspect-Oriented Programming
- **`aspect/LoggingAspect.java`**: AOP aspect providing cross-cutting concerns including method entry/exit logging, exception handling, and execution time measurement for service classes.

### Configuration
- **`config/WebClientConfig.java`**: Configuration class for Spring WebClient, setting up HTTP client for external API calls to TIDAL.

### Controllers (REST API Endpoints)
- **`controller/AlbumController.java`**: REST controller handling album-related HTTP requests (CRUD operations, search).
- **`controller/ArtistController.java`**: REST controller handling artist-related HTTP requests (CRUD operations, search).
- **`controller/HealthController.java`**: REST controller providing health check endpoints for application and TIDAL service monitoring.
- **`controller/SyncController.java`**: REST controller managing data synchronization operations with TIDAL API.

### Data Transfer Objects (DTOs)
Internal API DTOs:
- **`dto/AlbumDto.java`**: Response DTO for album data with artist information.
- **`dto/AlbumRequest.java`**: Request DTO for album creation/update operations.
- **`dto/ArtistDto.java`**: Response DTO for artist data.
- **`dto/ArtistRequest.java`**: Request DTO for artist creation/update operations.

TIDAL API Integration DTOs:
- **`dto/TidalAlbumAttributes.java`**: DTO for TIDAL album attributes (title).
- **`dto/TidalAlbumData.java`**: DTO wrapping TIDAL album data with ID and attributes.
- **`dto/TidalAlbumRelationshipResponse.java`**: DTO for TIDAL album relationship responses with data and included resources.
- **`dto/TidalAlbumResponse.java`**: Simple DTO for TIDAL album responses.
- **`dto/TidalArtistAttributes.java`**: DTO for TIDAL artist attributes (name).
- **`dto/TidalArtistData.java`**: DTO wrapping TIDAL artist data with ID and attributes.
- **`dto/TidalArtistEntityResponse.java`**: DTO for single TIDAL artist entity responses.
- **`dto/TidalArtistResponse.java`**: Simple DTO for TIDAL artist responses.
- **`dto/TidalHealthResponse.java`**: DTO for TIDAL service health check responses.
- **`dto/TidalSearchArtists.java`**: DTO for TIDAL artist search results.
- **`dto/TidalSearchResponse.java`**: DTO for TIDAL search API responses.
- **`dto/TidalTokenResponse.java`**: DTO for TIDAL OAuth token responses.

### Entity Models
- **`entity/Album.java`**: JPA entity representing albums in the database with relationships to artists.
- **`entity/Artist.java`**: JPA entity representing artists in the database.

### Data Access Layer
- **`repositories/AlbumRepository.java`**: Spring Data JPA repository interface for album data access with custom query methods.
- **`repositories/ArtistRepository.java`**: Spring Data JPA repository interface for artist data access with custom query methods.

### Scheduling
- **`scheduler/SyncScheduler.java`**: Spring scheduler component for automated data synchronization with TIDAL API.

### Business Logic Services
- **`service/AlbumService.java`**: Service class containing album-related business logic, data processing, and TIDAL integration.
- **`service/ArtistService.java`**: Service class containing artist-related business logic, data processing, and TIDAL integration.
- **`service/SyncService.java`**: Service class handling data synchronization operations between local database and TIDAL API.
- **`service/TidalService.java`**: Service class managing interactions with TIDAL API, including authentication, search, and data retrieval.

## Resources (src/main/resources/)

### Configuration
- **`application.yaml`**: Spring Boot application configuration file with database settings, server ports, logging, and external API configurations.

### Database Migrations
- **`db/migration/V1__init.sql`**: Initial database schema creation script for artists and albums tables.
- **`db/migration/V2__album_bigint_and_search_support.sql`**: Database migration adding BIGINT support and search functionality.
- **`db/migration/V3__postgres_full_text_search.sql`**: Database migration implementing PostgreSQL full-text search capabilities.

## Test Code (src/test/java/com/immomio/tidal/music/)

### Controller Tests
- **`controller/AlbumControllerTest.java`**: Unit tests for AlbumController endpoints.
- **`controller/ArtistControllerTest.java`**: Unit tests for ArtistController endpoints.
- **`controller/HealthControllerTest.java`**: Unit tests for HealthController endpoints.

### Service Tests
- **`service/AlbumServiceTest.java`**: Unit tests for AlbumService business logic.
- **`service/ArtistServiceTest.java`**: Unit tests for ArtistService business logic.
- **`service/SyncServiceTest.java`**: Unit tests for SyncService synchronization logic.
- **`service/TidalServiceHealthTest.java`**: Unit tests for TidalService health check functionality.

## Build Artifacts (target/)

### Compiled Classes
- **`classes/`**: Contains all compiled .class files from source code, mirroring the source structure.
- **`test-classes/`**: Contains compiled test .class files.

### Maven Build Status
- **`maven-status/`**: Maven build status files tracking compilation inputs and outputs.

### Test Reports
- **`surefire-reports/`**: JUnit test execution reports in both .txt and .xml formats for each test class.

## Hidden/Configuration Directories

### Maven Wrapper
- **`.mvn/wrapper/maven-wrapper.properties`**: Configuration for Maven wrapper specifying the Maven version and download URL.

This comprehensive file listing ensures all components of the Music Service are properly documented for development, maintenance, and deployment purposes.
