package com.immomio.tidal.music;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Music Service.
 * This is a Spring Boot application that provides REST APIs for managing artists and albums,
 * with synchronization capabilities from the TIDAL music streaming service.
 * 
 * Features:
 * - JPA for data persistence with PostgreSQL
 * - Flyway for database migrations
 * - AOP for logging and cross-cutting concerns
 * - Scheduled tasks for automatic syncing
 * - WebFlux for reactive HTTP clients
 */
@SpringBootApplication
@EnableAspectJAutoProxy  // Enables Aspect-Oriented Programming (AOP) proxying
@EnableScheduling        // Enables scheduled task execution
public class MusicApplication {

	/**
	 * Main method to start the Spring Boot application.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(MusicApplication.class, args);
	}

}
