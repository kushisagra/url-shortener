package com.kushagra.urlshortner.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) Configuration
 *
 * This class configures how your API documentation looks and works.
 *
 * Key concepts:
 *
 * 1. INFO: Metadata about your API (title, description, version)
 *    This appears at the top of the Swagger UI page.
 *
 * 2. SECURITY SCHEME: Defines how authentication works.
 *    We configure JWT Bearer token authentication here.
 *    Users can click "Authorize" button and paste their JWT token.
 *
 * 3. SERVERS: Lists the environments where your API runs.
 *    Useful for switching between local/staging/production.
 *
 * After running your app, access Swagger UI at:
 *   http://localhost:8080/swagger-ui.html
 *
 * Access raw OpenAPI JSON at:
 *   http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        // Define the security scheme name (referenced in controllers)
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // API Information
                .info(new Info()
                        .title("URL Shortener API")
                        .description("""
                                A robust URL shortening service with the following features:
                                
                                **Features:**
                                - 🔗 Shorten long URLs to compact, shareable links
                                - 🔒 JWT-based authentication for secure access
                                - 📊 Analytics tracking (clicks, devices, browsers, countries)
                                - ⏰ Optional URL expiration
                                - 🎯 Custom alias support
                                - 🚦 Rate limiting (10 requests/minute per IP)
                                
                                **Authentication:**
                                1. Register or Login to get a JWT token
                                2. Click the 'Authorize' button above
                                3. Enter: `Bearer <your_token>`
                                4. Now you can access protected endpoints
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Kushagra")
                                .email("kushagra@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // Server environments
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description("Current Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development")
                ))

                // Security configuration for JWT
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token. Get it from /api/auth/login or /api/auth/register")))

                // Apply security globally (can be overridden per endpoint)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}

