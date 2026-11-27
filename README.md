# Fundoo Notes Backend

A backend application for a note-taking system inspired by Google Keep, developed using **Java and Spring Boot**.

This project is part of the Bridgelabz Java Backend training program. It is developed feature by feature using Git branches, with each feature implemented, tested, and integrated into the development branch.

The project is divided into two parts, covering core Spring Boot backend development and advanced enterprise application concepts.

---

## Project Overview

Fundoo Notes Backend provides REST APIs for user management, authentication, note management, labels, password recovery, exception handling, application logging, messaging, caching, batch processing, owner-scoped reminders, CORS-controlled frontend access, and advanced architecture integration.

The application follows a layered architecture to maintain separation of concerns, readability, maintainability, and scalability.

## Part 1 – Core Backend Development

Part 1 consists of the following eight feature modules:

| Feature | Feature Name | Description |
|---|---|---|
| 01 | Database Configuration | Configures database connectivity and Spring Data JPA integration. |
| 02 | User Management | Implements user registration and user-related operations. |
| 03 | Authentication & Security | Implements authentication, password encoding, JWT-based security, and secured endpoints. |
| 04 | Note Management | Provides operations to create, retrieve, update, delete, pin, archive, trash, and restore notes. |
| 05 | Label & Search Management | Supports label management and searching notes. |
| 06 | Password Recovery | Implements the password recovery workflow. |
| 07 | Global Exception Handling | Provides centralized exception handling and consistent error responses. |
| 08 | Logging & Debugging | Implements request logging, service-level logging, exception logging, and logging configuration. |

## Part 2 – Advanced Spring Boot Concepts

Part 2 extends the application with messaging systems, caching, batch processing, aspect-oriented programming, frontend access control, reminders, and advanced architecture integration.

It consists of the following nine feature modules:

| Feature | Feature Name | Description |
|---|---|---|
| 09 | JMS Messaging | Introduces Java Message Service (JMS) for asynchronous communication using message producers and consumers. |
| 10 | RabbitMQ Integration | Integrates RabbitMQ for message-based communication using exchanges, queues, bindings, and consumers. |
| 11 | Redis Configuration & Integration | Integrates Redis for in-memory data storage, token management, and temporary data handling. |
| 12 | Caching Implementation | Implements application-level caching to improve performance and reduce repeated database operations. |
| 13 | Spring Batch & Excel Processing | Introduces batch processing for handling large datasets and Excel-based data import/export operations. |
| 14 | Aspect-Oriented Programming (AOP) | Implements aspects, pointcuts, and advice for cross-cutting concerns such as logging, monitoring, and execution tracking. |
| 15 | Advanced Architecture Integration | Integrates and verifies the advanced components, strengthens cross-feature communication, and explores Spring Cloud and distributed architecture concepts. |
| 16 | CORS Configuration | Restricts browser access to the configured React frontend origin without weakening JWT protection. |
| 17 | Reminder Integration | Adds owner-scoped note reminders with `Asia/Kolkata` validation, RabbitMQ wake-ups, and MySQL-backed recovery. |

### Part 2 – Key Concepts

- Asynchronous communication using JMS.
- Message publishing and consumption using RabbitMQ.
- Redis integration for temporary and frequently accessed data.
- Cache management, eviction, and consistency.
- Batch jobs and Excel processing workflows.
- Cross-cutting concerns using Spring AOP.
- Origin-based CORS configuration for the React frontend.
- Owner-scoped reminders with RabbitMQ wake-ups and database-backed recovery.
- Integration testing across multiple application components.
- Spring Cloud concepts and advanced application architecture.

## Technology Stack

| Category | Technologies |
|---|---|
| Programming Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build Tool | Maven |
| Database | MySQL |
| Database Access | Spring Data JPA |
| ORM | Hibernate |
| Security | Spring Security |
| Authentication | JSON Web Token (JWT) |
| Password Security | Password Encoding |
| Validation | Jakarta Bean Validation |
| Frontend Access | CORS |
| Messaging | JMS, ActiveMQ, RabbitMQ |
| Scheduling | Spring Scheduler |
| In-memory Data Store | Redis |
| Caching | Spring Cache |
| Batch Processing | Spring Batch |
| Excel Processing | Apache POI |
| Aspect-Oriented Programming | Spring AOP |
| Logging | SLF4J and Logback |
| Testing | JUnit 5 and Spring Boot Test |
| Version Control | Git and GitHub |

## Application Architecture

The application follows a layered architecture with supporting components for security, messaging, caching, and batch processing.

```text
                    Client
                      |
                      v
              REST Controllers
                      |
                      v
             Request DTO Validation
                      |
                      v
                Service Layer
                      |
             +--------+--------+
             |                 |
             v                 v
       Cache Layer        Messaging Layer
             |             /          \
             v            v            v
          Redis          JMS        RabbitMQ
             |
             v
       Repository Layer
             |
             v
           MySQL
```

### Supporting Components

- **DTOs:** Transfer request and response data between the client and application.
- **Security Configuration:** Manages authentication and authorization.
- **JWT Components:** Handle token generation and validation.
- **Redis Integration:** Supports token management and temporary data storage.
- **Caching:** Reduces repeated database operations.
- **Messaging:** Supports asynchronous communication using JMS and RabbitMQ.
- **CORS Configuration:** Restricts browser access to the configured frontend origin.
- **Reminder Components:** Persist reminders, publish RabbitMQ wake-ups, and recover due reminders from MySQL.
- **Spring Batch:** Handles batch-oriented processing and Excel workflows.
- **AOP:** Manages cross-cutting concerns such as logging and monitoring.
- **Exception Handler:** Handles application exceptions centrally.
- **Logging Interceptor:** Records incoming requests and completed responses.
- **Application Configuration:** Manages environment-specific application settings.

## Project Structure

```text
fundoo-notes-backend/
│
├── src/
│   ├── main/
│   │   ├── java/com/fundoonotes/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── event/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   ├── redis/
│   │   │   ├── messaging/
│   │   │   ├── cache/
│   │   │   ├── batch/
│   │   │   ├── aspect/
│   │   │   ├── util/
│   │   │   ├── validation/
│   │   │   └── FundooNotesBackendApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/fundoonotes/
│
├── .gitignore
├── pom.xml
└── README.md
```

*The exact package contents may vary according to implementation and may grow as the application develops.*

## Prerequisites

Install the following before running the application:

- Java Development Kit (JDK) 21
- Apache Maven
- Git
- MySQL
- Redis
- RabbitMQ
- ActiveMQ or another compatible JMS provider
- An IDE such as IntelliJ IDEA, Eclipse, or VS Code

Additional infrastructure services may be required depending on the feature being executed.

## Configuration

Before running the application, configure the database and other required environment-specific properties in:

```text
src/main/resources/application.properties
```

Configure the required connection details for:

- MySQL database.
- Redis server.
- RabbitMQ broker.
- JMS provider.
- Mail server for password recovery and reminders.
- JWT secret and token configuration through `JWT_SECRET` (no default).
- Frontend origin used by CORS, `http://localhost:5173` by default.
- Password reset frontend URL through `PASSWORD_RESET_URL`, `http://localhost:5173` by default.
- Reminder poll interval and `MAIL_FROM` sender address.

Do not commit actual database passwords, mail credentials, JWT secrets, or other sensitive values to the repository.

Use environment variables or an appropriate external configuration mechanism to manage sensitive information.

Reminder times are interpreted in `Asia/Kolkata`. SMTP acceptance and the database status update are not atomic, so duplicate reminder emails are possible after an ambiguous SMTP failure.

## Running the Application

Clone the repository:

```bash
git clone https://github.com/Hemang71006/BL_FundooNotes_Backend.git
```

Navigate into the project:

```bash
cd BL_FundooNotes_Backend
```

Build the project:

```bash
mvn clean install
```

Run the application:

```bash
mvn spring-boot:run
```

Ensure that the required external services are configured and running before executing the corresponding features.

## Running Tests

Execute the complete test suite:

```bash
mvn clean test
```

For full verification, including packaging:

```bash
mvn clean verify
```

To build the project and verify that tests pass:

```bash
mvn clean install
```

## Git Branching Strategy

The project follows a feature-based Git workflow.

- `main` – Main project branch.
- `develop` – Integration branch for completed features.
- `feature/*` – Individual feature development branches.

### Part 1 Feature Branches

```text
main
  |
  └── develop
        |
        ├── feature/01-database-configuration
        ├── feature/02-user-management
        ├── feature/03-authentication-security
        ├── feature/04-note-management
        ├── feature/05-label-search-management
        ├── feature/06-password-recovery
        ├── feature/07-global-exception-handling
        └── feature/08-logging-debugging
```

### Part 2 Feature Branches

```text
develop
  |
  ├── feature/09-jms-messaging
  ├── feature/10-rabbitmq-integration
  ├── feature/11-redis-integration
  ├── feature/12-caching-implementation
  ├── feature/13-spring-batch-excel
  ├── feature/14-aop-implementation
  ├── feature/15-advanced-architecture-integration
  ├── feature/16-cors-configuration
  └── feature/17-reminder-integration
```

Each feature is developed and tested independently before being integrated into the `develop` branch.

## Learning Outcomes

### Part 1 – Core Backend Development

- Understanding Spring Boot application architecture.
- Configuring database connectivity.
- Implementing RESTful APIs.
- Understanding dependency injection and layered architecture.
- Working with entities, repositories, and DTOs.
- Implementing user registration and management.
- Understanding authentication and authorization.
- Implementing JWT-based security.
- Performing note and label operations.
- Implementing password recovery.
- Validating incoming request data.
- Handling exceptions globally.
- Configuring application logging.
- Writing and executing unit and integration tests.
- Managing feature branches and merging changes using Git.

### Part 2 – Advanced Backend Development

- Understanding asynchronous communication using JMS.
- Implementing message producers and consumers.
- Integrating RabbitMQ with Spring Boot.
- Understanding exchanges, queues, and bindings.
- Integrating Redis with Spring Boot.
- Managing temporary data and token information using Redis.
- Implementing caching with Spring Cache.
- Understanding cache eviction and invalidation.
- Creating batch jobs using Spring Batch.
- Processing Excel files using Apache POI.
- Understanding AOP, aspects, pointcuts, and advice.
- Implementing cross-cutting concerns.
- Configuring origin-based CORS for a React frontend.
- Implementing owner-scoped reminders with RabbitMQ wake-ups and database-backed recovery.
- Understanding Spring Cloud and distributed application architecture.
- Integrating and testing multiple application components.

## Author

**Hemang**

**Repository:** https://github.com/Hemang71006/BL_FundooNotes_Backend