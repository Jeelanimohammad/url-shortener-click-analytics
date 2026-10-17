package com.urlshortener.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Holds application-wide configuration values.
 *
 * @Configuration → Spring treats this as a bean-definition class.
 * @Value("${...}") → injects the matching property from application.properties
 *                    (or an environment variable that overrides it).
 *
 * WHY A SEPARATE CONFIG CLASS?
 *   If we hardcoded "http://localhost:8080" in the service, changing the base
 *   URL for production would require a code change and rebuild.  Externalising
 *   it here means we can override it with a single environment variable.
 */
@Configuration
public class AppConfig {

    /**
     * The publicly accessible root URL of this service.
     * Injected from application.properties → app.base-url
     * or the environment variable BASE_URL.
     *
     * Example values:
     *   Development  → http://localhost:8080
     *   Production   → https://short.example.com
     */
    @Value("${app.base-url}")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }
}
