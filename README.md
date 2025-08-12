# Account Service

A Spring Boot microservice responsible for bank account creation, management, and operations within a secure banking platform. This service integrates with JWT-based authentication and event-driven architecture using Apache Kafka for automated account provisioning.

## Overview

The Account Service is a critical component of the banking microservices ecosystem, providing comprehensive account lifecycle management capabilities. It handles account creation, balance management, status updates, and integrates with Customer and KYC services through secure APIs and event messaging.

### Key Features

- **Account Creation**: Support for Current and Savings accounts with validation
- **Balance Management**: Real-time balance tracking with administrative controls
- **Status Management**: Account activation, suspension, and closure workflows
- **JWT Authentication**: Role-based access control with Bearer token validation
- **Event-Driven Integration**: Kafka integration for automated account creation
- **Admin Operations**: Comprehensive administrative endpoints for account oversight
- **Multi-Account Support**: Single customer can maintain both Current and Savings accounts
- **Inter-Service Communication**: Secure integration with Customer and KYC services

## Architecture

The service follows a layered architecture pattern with event-driven integration:

```
┌─────────────────┐
│   Controllers   │ ← REST API Layer
├─────────────────┤
│    Services     │ ← Business Logic Layer
├─────────────────┤
│   Repositories  │ ← Data Access Layer
├─────────────────┤
│    Database     │ ← Oracle 23c
└─────────────────┘
        ↕
┌─────────────────┐
│ Kafka Consumer  │ ← Event Processing
└─────────────────┘
```

### Inter-Service Communication

- **Inbound**: Web Gateway (JWT-secured requests)
- **Inbound**: Kafka events from Customer Service (KYC completion)
- **Outbound**: Customer Service API calls for validation
- **Internal**: Account ownership verification and balance operations

### Event-Driven Account Creation Flow

```
┌─────────────┐    KYC Status     ┌─────────────────┐    Kafka Event    ┌─────────────────┐
│ KYC Service │ ──── Update ────→ │ Customer Service │ ──── Publish ───→ │ Account Service │
│   (8084)    │                  │     (8081)      │                   │     (8083)      │
└─────────────┘                  └─────────────────┘                   └─────────────────┘
                                           │                                       │
                                           │                                       │
                                      Updates KYC                          Creates Both
                                      Status to                           Current & Savings
                                      "VERIFIED"                              Accounts
```

**Flow Description:**
1. KYC Service (8084) completes customer verification
2. KYC Service calls Customer Service (8081) to update KYC status to "VERIFIED"
3. Customer Service publishes "account-creation-topic" Kafka event
4. Account Service (8083) consumes the event and automatically creates both account types
5. Customer receives instant access to banking services upon KYC completion

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Runtime | Java | 17+ |
| Framework | Spring Boot | 3.x |
| Web | Spring Web MVC | 3.x |
| Security | Spring Security | 3.x |
| Data Access | Spring Data JPA | 3.x |
| Database | Oracle Database | 23c |
| Migration | Flyway | Latest |
| Mapping | MapStruct | Latest |
| Utilities | Lombok | Latest |
| Messaging | Apache Kafka | Latest |
| Authentication | JWT (jjwt) | Latest |
| Documentation | SpringDoc OpenAPI | Latest |
| Connection Pooling | HikariCP | Latest |

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.6+
- Oracle Database 23c
- Apache Kafka 2.8+
- Customer Service (8081) running
- KYC Service (8084) running
- Gateway Service (8080) running
- Git

## Installation & Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd account-service
```

### 2. Database Setup

Ensure Oracle Database is running and accessible. The Customer Service database must be available as Account Service references the CUSTOMERS table:

```sql
-- Ensure Customer Service schema exists
-- Account Service will reference CUSTOMERS table via foreign key
```

### 3. Kafka Setup

Start Kafka services:

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka Server
bin/kafka-server-start.sh config/server.properties

# Create required topic
bin/kafka-topics.sh --create --topic account-creation-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

### 4. Service Dependencies

Ensure dependent services are running:

```bash
# Customer Service (Port 8081)
# KYC Service (Port 8084)
# Gateway Service (Port 8080)
```

### 5. Build Application

```bash
./mvnw clean compile
```

### 6. Run Application

```bash
./mvnw spring-boot:run
```

The application will start on port 8083.

## Configuration

### Application Configuration (application.yml)

```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/FREEPDB1
    username: RAGHUL
    password: your_password
    driver-class-name: oracle.jdbc.OracleDriver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  
  flyway:
    enabled: true
    table: flyway_schema_history_account_service
    locations: classpath:db/migration
    baseline-on-migrate: true
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: account-service-group

jwt:
  secret: your_base64_encoded_secret_key
  expiration-ms: 3600000

services:
  customer:
    base-url: http://localhost:8081
  kyc:
    base-url: http://localhost:8084
  gateway:
    base-url: http://localhost:8080

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Oracle database connection URL | `jdbc:oracle:thin:@localhost:1521/FREEPDB1` |
| `DB_USERNAME` | Database username | `RAGHUL` |
| `DB_PASSWORD` | Database password | Required |
| `KAFKA_SERVERS` | Kafka bootstrap servers | `localhost:9092` |
| `JWT_SECRET` | JWT signing secret (Base64) | Required |
| `CUSTOMER_SERVICE_URL` | Customer Service base URL | `http://localhost:8081` |

## API Documentation

### Authentication

All endpoints require JWT authentication via the `Authorization` header:

```
Authorization: Bearer <jwt_token>
```

### Customer Operations

#### Create Account

**POST** `/api/accounts/create`

Creates a new account for the authenticated customer. Supports both Current and Savings account types.

**Request Body:**
```json
{
  "customerId": 1,
  "accountType": "CURRENT",
  "initialBalance": 5000.00
}
```

**Response:**
```json
{
  "accountId": 1,
  "customerId": 1,
  "accountNumber": "BANK1CUR001",
  "accountType": "CURRENT",
  "accountStatus": "ACTIVE",
  "balance": 5000.00,
  "formattedBalance": "₹5,000.00",
  "createdAt": "2024-01-15T10:30:00Z",
  "message": "Account created successfully"
}
```

#### Get My Accounts

**GET** `/api/accounts/my-accounts`

Retrieves all accounts for the authenticated customer.

#### Get Account Details

**GET** `/api/accounts/details/{accountId}`

Retrieves specific account information with ownership verification.

#### Get Account by Type

**GET** `/api/accounts/my-accounts/{accountType}`

Retrieves specific account type (CURRENT or SAVINGS) for the customer.

### Administrative Operations

#### Get All Accounts

**GET** `/api/accounts/admin/all`

Returns all accounts in the system. Requires ADMIN role.

#### Get Customer Accounts

**GET** `/api/accounts/admin/customer/{customerId}`

Returns all accounts for a specific customer.

#### Update Account Status

**PUT** `/api/accounts/admin/{accountId}/status`

Updates account status (ACTIVE, SUSPENDED, CLOSED).

**Request Body:**
```json
{
  "accountStatus": "SUSPENDED",
  "reason": "Account under review"
}
```

#### Update Account Balance

**PUT** `/api/accounts/admin/{accountId}/balance`

Updates account balance with administrative override.

**Request Body:**
```json
{
  "balance": 75000.00,
  "reason": "Balance correction by admin"
}
```

#### Get Account Statistics

**GET** `/api/accounts/admin/stats`

Returns comprehensive account statistics including totals, balances, and status distribution.

#### Manual Account Creation

**POST** `/api/accounts/admin/create-for-customer/{customerId}`

Manually triggers account creation for a customer (bypasses Kafka flow).

### API Documentation Access

- Swagger UI: `http://localhost:8083/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8083/v3/api-docs`

## Database Schema

### Accounts Table

```sql
CREATE TABLE ACCOUNTS (
    account_id NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY PRIMARY KEY,
    customer_id NUMBER NOT NULL,
    account_number VARCHAR2(20) UNIQUE NOT NULL,
    account_type VARCHAR2(10) NOT NULL,
    account_status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,
    balance NUMBER(15,2) DEFAULT 0.00 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id)
        REFERENCES CUSTOMERS (customer_id) ON DELETE CASCADE,
    CONSTRAINT uk_customer_account_type UNIQUE (customer_id, account_type),
    CONSTRAINT chk_account_type CHECK (account_type IN ('CURRENT', 'SAVINGS')),
    CONSTRAINT chk_account_status CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);
```

### Data Validation Rules

- **Account Number**: Generated format `BANK1CUR001`, `BANK1SAV001`
- **Account Type**: Must be CURRENT or SAVINGS
- **Account Status**: ACTIVE, SUSPENDED, or CLOSED
- **Balance**: Must be non-negative (≥ 0.00)
- **Uniqueness**: One customer can have maximum one account per type

## Security Implementation

### JWT Authentication

The service implements comprehensive JWT-based authentication:

- **Token Validation**: All protected endpoints validate JWT tokens
- **Role-Based Access**: CUSTOMER and ADMIN roles with different permissions
- **User Context**: Authenticated user information available via ThreadLocal
- **Access Control**: Customers can only access their own accounts
- **Admin Override**: ADMIN role can access all accounts and perform administrative operations

### Security Filter Chain

The `JwtAuthenticationFilter` handles:
- Token extraction from Authorization header
- Token validation using HMAC-SHA256
- User context establishment in ThreadLocal
- Spring Security context population
- Role-based authorization enforcement

### Protected Endpoints

All endpoints except health checks and documentation require valid JWT authentication.

## Event-Driven Integration

### Kafka Integration

The service consumes events from Apache Kafka for automated account creation:

**Topic**: `account-creation-topic`

**Event Structure**:
```json
{
  "customerId": 123,
  "eventType": "KYC_COMPLETED",
  "timestamp": "2024-01-15T10:30:00Z",
  "source": "customer-service"
}
```

### Event Processing Flow

1. **KYC Completion**: KYC Service verifies customer documents
2. **Status Update**: KYC Service calls Customer Service to update status to VERIFIED
3. **Event Publication**: Customer Service publishes Kafka event to `account-creation-topic`
4. **Event Consumption**: Account Service consumes event via `KafkaAccountCreationConsumer`
5. **Account Creation**: Service automatically creates both Current and Savings accounts
6. **Customer Notification**: Accounts are immediately available for customer use

### Event Consumer Configuration

```java
@KafkaListener(topics = "account-creation-topic", groupId = "account-service-group")
public void consume(String message) {
    // Process account creation event
    // Create both CURRENT and SAVINGS accounts
    // Handle duplicate prevention
}
```

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Integration tests
./mvnw verify
```

### Postman Collection

Import the provided Postman collection for comprehensive API testing:

**Environment Variables:**
- `base_url`: `http://localhost:8083`
- `gateway_url`: `http://localhost:8080`
- `jwt_token`: `<your-jwt-token>`
- `admin_jwt_token`: `<admin-jwt-token>`

### Sample Test Scenarios

1. **Account Creation Flow**
   - Authenticate as customer
   - Create Current account
   - Create Savings account
   - Verify duplicate prevention

2. **Account Management Flow**
   - Retrieve account details
   - Check balance information
   - Verify ownership controls

3. **Admin Operations Flow**
   - Authenticate as admin
   - View all accounts
   - Update account status
   - Modify account balance
   - Generate statistics

4. **Event-Driven Flow**
   - Trigger KYC completion
   - Verify Kafka event consumption
   - Confirm automatic account creation

## Project Structure

```
src/main/java/com/bank/account/
├── config/                    # Configuration classes
│   ├── CorsConfig.java
│   ├── KafkaConsumerConfig.java
│   ├── RestConfig.java
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── WebConfig.java
├── controller/                # REST controllers
│   ├── AccountController.java
│   └── AccountAdminController.java
├── dto/                      # Data transfer objects
│   ├── AccountCreationRequest.java
│   ├── AccountResponse.java
│   ├── AccountStatsResponse.java
│   ├── AccountStatusUpdateRequest.java
│   ├── BalanceUpdateRequest.java
│   └── KycCompletedEvent.java
├── entity/                   # JPA entities
│   ├── Account.java
│   └── AccountStatus.java
├── exception/                # Exception handling
│   ├── GlobalExceptionHandler.java
│   ├── AccountNotFoundException.java
│   ├── DuplicateAccountException.java
│   ├── InsufficientBalanceException.java
│   └── InvalidAccountOperationException.java
├── kafka/                    # Kafka integration
│   └── KafkaAccountCreationConsumer.java
├── client/                   # External service clients
│   └── CustomerServiceClient.java
├── repository/               # Data repositories
│   └── AccountRepository.java
├── security/                 # Security components
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthInterceptor.java
│   └── JwtUtils.java
├── service/                  # Business logic
│   ├── AccountService.java
│   ├── AdminAuthService.java
│   └── impl/
│       ├── AccountServiceImpl.java
│       └── AdminAuthServiceImpl.java
├── util/                     # Utility classes
│   ├── AccountNumberGenerator.java
│   ├── AccountType.java
│   └── AuthenticatedUser.java
└── AccountServiceApplication.java
```

## Error Handling

The service implements comprehensive error handling with specific exception types:

### Exception Types

- **AccountNotFoundException**: When requested account is not found
- **DuplicateAccountException**: When attempting to create duplicate account type
- **InsufficientBalanceException**: For insufficient balance operations
- **InvalidAccountOperationException**: For invalid account operations
- **SecurityException**: For authorization failures

### Error Response Format

```json
{
  "error": {
    "code": "ACCOUNT_NOT_FOUND",
    "message": "Account not found with ID: 123",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

## Logging

### Logging Configuration

The service uses SLF4J with Logback for structured logging:

- **INFO**: Business operations and service interactions
- **DEBUG**: Detailed tracing including JWT processing
- **ERROR**: Exception handling and system errors
- **WARN**: Non-critical issues and validation warnings

### Key Log Messages

- Account creation and management operations
- JWT authentication and authorization
- Kafka event processing
- Inter-service communication
- Database operations and constraints

## Integration Points

### External Dependencies

1. **Customer Service (8081)**: Customer validation and resolution
2. **KYC Service (8084)**: KYC status verification (via Customer Service)
3. **Gateway Service (8080)**: Authentication and request routing
4. **Oracle Database**: Primary data storage with foreign key to Customer Service
5. **Apache Kafka**: Event messaging for automated account creation

### Internal APIs

The service exposes several internal integration points:
- Account ownership verification
- Balance inquiry and management
- Account status management
- Statistical reporting

## Data Migration

Database schema is managed using Flyway migrations:

- **V1__Create_accounts_table.sql**: Account table creation with constraints and indexes

### Migration Commands

```bash
# Check migration status
./mvnw flyway:info

# Apply migrations
./mvnw flyway:migrate

# Validate schema
./mvnw flyway:validate
```

## Monitoring and Observability

### Health Checks

- **Application Health**: `/actuator/health`
- **Database Connectivity**: Automated health indicators
- **Kafka Connectivity**: Consumer group status

### Metrics

- Account creation rates
- Balance distribution statistics
- API response times
- Error rates by endpoint

## Troubleshooting

### Common Issues

1. **Database Connection Failures**
   - Verify Oracle service is running
   - Check connection parameters and credentials
   - Validate foreign key references to Customer Service

2. **JWT Authentication Errors**
   - Verify JWT secret matches other services
   - Check token expiration settings
   - Validate role claims in token

3. **Kafka Integration Issues**
   - Ensure Kafka broker is accessible
   - Verify topic creation and permissions
   - Check consumer group configuration

4. **Inter-Service Communication**
   - Verify Customer Service availability
   - Check network connectivity between services
   - Validate API endpoint configurations

### Debug Mode

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    com.bank.account: DEBUG
    org.springframework.kafka: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```
