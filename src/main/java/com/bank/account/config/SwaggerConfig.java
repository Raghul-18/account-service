package com.bank.account.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8083}")
    private String serverPort;

    @Value("${spring.application.name:account-service}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("🔧 Configuring Swagger OpenAPI documentation for Account Service");

        return new OpenAPI()
                .info(getApiInfo())
                .servers(getServers())
                .components(getComponents())
                .security(getSecurityRequirements());
    }

    private Info getApiInfo() {
        return new Info()
                .title("🏦 Bank Account Service API")
                .version("1.0.0")
                .description("""
                        ## Bank Account Management System
                        
                        This API provides comprehensive account management functionality for a banking system.
                        
                        ### Features
                        - **Account Creation**: Create Current and Savings accounts
                        - **Balance Management**: View and update account balances (Admin)
                        - **Account Status Management**: Activate, suspend, or close accounts (Admin)
                        - **Multi-role Support**: Customer and Admin access levels
                        - **JWT Authentication**: Secure API access with role-based authorization
                        - **Inter-service Communication**: Integration with Customer and KYC services
                        
                        ### Account Types
                        - **CURRENT**: Business accounts with higher transaction limits
                        - **SAVINGS**: Personal savings accounts with interest
                        
                        ### Security
                        All endpoints require JWT authentication with appropriate roles:
                        - **CUSTOMER**: Can manage their own accounts
                        - **ADMIN**: Full system access including all customer accounts
                        
                        ### Getting Started
                        1. Obtain JWT token from Auth Service
                        2. Include token in Authorization header: `Bearer <your-jwt-token>`
                        3. Use appropriate endpoints based on your role
                        """);
    }

    private List<Server> getServers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
                new Server()
                        .url("http://localhost:8080/api/account")
                        .description("API Gateway (Recommended)")
        );
    }

    private Components getComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Authorization header using the Bearer scheme. Example: 'Bearer {token}'"));
    }

    private List<SecurityRequirement> getSecurityRequirements() {
        return List.of(new SecurityRequirement().addList("bearerAuth"));
    }
}