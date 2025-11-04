package dev.kuku.authsome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Authsome Core Spring Boot application.
 */
@SpringBootApplication
public class AuthsomeCoreApplication {

    /**
     * Starts the Authsome Core Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthsomeCoreApplication.class, args);
    }

    //TODO define rest endpoint
}
