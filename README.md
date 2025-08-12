# Account Service

A Spring Boot microservice for managing bank accounts in a secure onboarding banking system. The service provides comprehensive account management functionality with JWT-based authentication, role-based authorization, and inter-service communication capabilities.

## Overview

The Account Service is responsible for:
- Creating and managing customer bank accounts (Current and Savings)
- Balance management and account status operations
- Multi-role access control (Customer and Admin)
- Automated account creation through Kafka integration
- Inter-service communication with Customer and KYC services

## Features

### Core Functionality
- **Account Creation**: Support for Current and Savings account types
- **Balance Management**: View balances with formatted currency display
- **Account Status Management**: Activate, suspend, or close accounts
- **Ownership Verification**: Ensure customers can only access their own accounts
- **Account Statistics**: Comprehensive reporting for administrative oversight

### Security
- **JWT Authentication**: Token-based authentication with role validation
- **Role-Based Authorization**: Separate access levels for customers and administrators
- **Cross-Origin Resource Sharing (CORS)**: Configured for frontend integration
- **Input Validation**: Comprehensive request validation with error handling

### Integration
- **Inter-Service Communication**: REST-based communication with Customer and KYC services
- **Kafka Integration**: Event-driven account creation upon KYC completion
- **API Gateway Support**: Designed to work with centralized API gateway

### Documentation
- **Swagger/OpenAPI**: Interactive API documentation
- **Health Monitoring**: Actuator endpoints for service health checks

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.x
- **Database**: Oracle Database with Flyway migrations
- **Security**: Spring Security with JWT
- **Messaging**: Apache Kafka
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven

### Database Schema
The service uses a single `ACCOUNTS` table with the following structure:
- `account_id`: Primary key (auto-generated)
- `customer_id`: Foreign key to Customer service
- `account_number`: Unique identifier (format: BANK1CUR001, BANK1SAV001)
- `account_type`: CURRENT or SAVINGS
- `account_status`: ACTIVE, SUSPENDED, or CLOSED
- `balance`: Current account balance (non-negative)
- `created_at`: Account creation timestamp

### Account Types
- **CURRENT**: Business accounts designed for frequent transactions
- **SAVINGS**: Personal savings accounts with interest earning potential

## API Endpoints

### Customer Endpoints
All customer endpoints require JWT authentication with CUSTOMER or ADMIN role.

#### Account Management
- `POST /api/accounts/create` - Create new account
- `GET /api/accounts/my-accounts` - Get all customer accounts
- `GET /api/accounts/my-accounts/{accountType}` - Get specific account type
- `GET /api/accounts/details/{accountId}` - Get account details

### Admin Endpoints
Admin endpoints require JWT authentication with ADMIN role.

#### Account Administration
- `GET /api/accounts/admin/all` - Get all accounts
- `GET /api/accounts/admin/customer/{customerId}` - Get customer's accounts
- `GET /api/accounts/admin/{accountId}` - Get account by ID
- `GET /api/accounts/admin/status/{status}` - Get accounts by status
- `GET /api/accounts/admin/stats` - Get account statistics

#### Account Operations
- `PUT /api/accounts/admin/{accountId}/status` - Update account status
- `PUT /api/accounts/admin/{accountId}/balance` - Update account balance
- `POST /api/accounts/admin/create-for-customer/{customerId}` - Create accounts for customer

### Public Endpoints
- `GET /actuator/health` - Service health check
- `GET /swagger-ui.html` - API documentation

## Configuration

### Environment Variables
The service requires the following configuration:

```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/FREEPDB1
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: oracle.jdbc.OracleDriver

jwt:
  secret: ${JWT_SECRET}
  expiration-ms: 3600000

services:
  customer:
    base-url: http://localhost:8081
  kyc:
    base-url: http://localhost:8084
  gateway:
    base-url: http://localhost:8080

kafka:
  bootstrap-servers: localhost:9092
```

### Database Configuration
- **Database**: Oracle Database 19c or higher
- **Migration Tool**: Flyway for database schema management
- **Connection Pool**: HikariCP with optimized settings

## Getting Started

### Prerequisites
- Java 17 or higher
- Oracle Database 19c or higher
- Apache Kafka 2.8 or higher
- Maven 3.6 or higher

### Installation
1. Clone the repository
2. Configure database connection in `application.yml`
3. Run Flyway migrations: `mvn flyway:migrate`
4. Start the application: `mvn spring-boot:run`

### Testing with Postman
A complete Postman collection is provided with pre-configured test scenarios:

#### Authentication Setup
1. Use "Login Customer (via Gateway)" to obtain customer JWT token
2. Use "Login Admin (via Gateway)" to obtain admin JWT token
3. Tokens are automatically saved as environment variables

#### Test Workflows
1. **Account Creation**: Create Current and Savings accounts
2. **Account Retrieval**: Fetch account details and listings
3. **Admin Operations**: Manage account status and balances
4. **Security Testing**: Verify access control and authorization
5. **Error Scenarios**: Test validation and error handling

## Security Model

### Authentication
- JWT tokens issued by Auth Service through API Gateway
- Token validation using shared secret key
- Automatic token extraction and user context setup

### Authorization
- **CUSTOMER Role**: Can create and view own accounts only
- **ADMIN Role**: Full system access including all customer operations
- Account ownership verification for customer access

### Data Protection
- No sensitive data stored in JWT tokens
- Customer ID resolution through secure service calls
- Account access restricted by ownership verification

## Inter-Service Communication

### Customer Service Integration
- `GET /api/customers/user/{userId}/customer-id` - Resolve user to customer mapping
- `GET /api/customers/{customerId}/verify-ownership/{userId}` - Verify ownership
- `GET /api/customers/{customerId}/status` - Check KYC verification status

### Event-Driven Architecture
- **Kafka Consumer**: Listens for KYC completion events
- **Automatic Processing**: Creates accounts when customer KYC is verified
- **Error Handling**: Comprehensive error management and logging

## Error Handling

### Exception Types
- `AccountNotFoundException`: Account not found scenarios
- `DuplicateAccountException`: Attempt to create duplicate account types
- `InsufficientBalanceException`: Balance-related operation failures
- `InvalidAccountOperationException`: Business rule violations
- `SecurityException`: Authentication and authorization failures

### Response Format
All errors return structured JSON responses:
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "timestamp": "2025-01-01T12:00:00"
  }
}
```

## Monitoring and Observability

### Health Checks
- Spring Boot Actuator endpoints for service monitoring
- Database connectivity verification
- Custom health indicators for external service dependencies

### Logging
- Structured logging with appropriate log levels
- Request/response logging for debugging
- Security event logging for audit trails

### API Documentation
- Interactive Swagger UI at `/swagger-ui.html`
- OpenAPI 3 specification at `/v3/api-docs`
- Comprehensive endpoint documentation with examples

## Development Guidelines

### Code Organization
- **Controllers**: Separate customer and admin controllers
- **Services**: Business logic implementation with interface abstractions
- **DTOs**: Request/response objects with validation annotations
- **Entities**: JPA entities with proper relationships and constraints
- **Utilities**: Helper classes for common operations

### Database Best Practices
- Flyway migrations for schema versioning
- Proper indexing for performance optimization
- Referential integrity with foreign key constraints
- Check constraints for data validation

### Testing Strategy
- Comprehensive Postman collection for API testing
- Security test scenarios for access control verification
- Error condition testing for robust error handling
- Integration testing with dependent services

## Deployment Considerations

### Environment Setup
- Oracle Database with proper schema and user permissions
- Kafka cluster for event processing
- Service discovery and registration with API Gateway
- Environment-specific configuration management

### Performance Optimization
- Database connection pooling with HikariCP
- Indexed queries for efficient data retrieval
- Caching strategies for frequently accessed data
- Asynchronous processing for non-critical operations

### Scalability
- Stateless service design for horizontal scaling
- Database connection pool sizing for concurrent load
- Kafka consumer group configuration for parallel processing
- Load balancing through API Gateway

## API Gateway Integration

The service is designed to work seamlessly with an API Gateway:
- All authentication flows through the gateway
- Consistent JWT token handling across services
- Centralized CORS and security policy management
- Service discovery and load balancing support
