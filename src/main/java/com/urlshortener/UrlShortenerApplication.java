package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the URL Shortener application.
 *
 * @SpringBootApplication combines three annotations:
 *   - @Configuration          → marks this class as a source of Spring beans
 *   - @EnableAutoConfiguration → tells Spring Boot to auto-configure the app
 *   - @ComponentScan          → scans sub-packages for controllers, services, etc.
 */
@SpringBootApplication
public class UrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}
