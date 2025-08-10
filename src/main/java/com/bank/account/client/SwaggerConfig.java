package com.bank.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for Account Service
 * Provides comprehensive API documentation with JWT authentication
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8083}")
    private int serverPort;

    @Value("${spring.application.name:account-service}")
    private String applicationName;

    /**
     * Configure OpenAPI documentation
     */
    @Bean
    public OpenAPI accountServiceOpenAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .servers(getServers())
                .components(getComponents())
                .addSecurityItem(getSecurityRequirement());
    }

    /**
     * API Information
     */
    private Info getApiInfo() {
        return new Info()
                .title("Account Service API")
                .description("""
                    ## Account Management Microservice
                    
                    This service manages bank accounts, balances, and account operations.
                    
                    ### Key Features:
                    - **Account Management**: Create, read, update accounts
                    - **Balance Operations**: Check balances, update balances (admin)
                    - **Status Management**: Activate, deactivate, close accounts
                    - **Security**: JWT-based authentication and authorization
                    - **Business Rules**: Minimum balance, account type limits
                    - **Admin Operations**: Statistics, bulk operations, system management
                    
                    ### Authentication:
                    All endpoints require JWT authentication. Include the token in the Authorization header:
                    ```
                    Authorization: Bearer <your-jwt-token>
                    ```
                    
                    ### User Roles:
                    - **CUSTOMER**: Can manage their own accounts only
                    - **ADMIN**: Can manage all accounts and perform admin operations
                    
                    ### Account Types:
                    - **SAVINGS**: Minimum balance ₹1,000
                    - **CURRENT**: Minimum balance ₹5,000
                    
                    ### Account Status:
                    - **ACTIVE**: Normal operations allowed
                    - **INACTIVE**: No transactions, can be reactivated
                    - **SUSPENDED**: Temporarily blocked, admin can reactivate
                    - **FROZEN**: Frozen by admin, manual intervention required
                    - **CLOSED**: Permanently closed, cannot be reactivated
                    """)
                .version("1.0.0")
                .contact(getContact())
                .license(getLicense());
    }

    /**
     * Contact Information
     */
    private Contact getContact() {
        return new Contact()
                .name("Banking System Team")
                .email("support@bankingsystem.com")
                .url("https://bankingsystem.com");
    }

    /**
     * License Information
     */
    private License getLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Server Configuration
     */
    private List<Server> getServers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
                new Server()
                        .url("http://localhost:8080/account-service")
                        .description("API Gateway"),
                new Server()
                        .url("https://api.bankingsystem.com/account-service")
                        .description("Production Server")
        );
    }

    /**
     * Security Components
     */
    private Components getComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                            JWT Authorization header using the Bearer scheme.
                            
                            Enter 'Bearer' [space] and then your token in the text input below.
                            
                            Example: "Bearer 12345abcdef"
                            """));
    }

    /**
     * Security Requirement
     */
    private SecurityRequirement getSecurityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }
}