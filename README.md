# Fundoo Notes Backend

A backend application for a note-taking system inspired by Google Keep, developed using **Java and Spring Boot**.

This project is part of the Bridgelabz Java Backend training program. It is developed feature by feature using Git branches, with each feature implemented, tested, and integrated into the development branch.

## Project Overview

Fundoo Notes Backend provides REST APIs for user management, authentication, note management, labels, password recovery, exception handling, and application logging.

The project follows a layered architecture to maintain separation of concerns, readability, maintainability, and scalability.

## Part 1 – Completed Features

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

## Technology Stack

- **Programming Language:** Java 21
- **Framework:** Spring Boot 4.1.2
- **Build Tool:** Maven
- **Database Access:** Spring Data JPA
- **ORM:** Hibernate
- **Security:** Spring Security
- **Authentication:** JSON Web Token (JWT)
- **Password Security:** Password Encoding
- **Validation:** Jakarta Bean Validation
- **Logging:** SLF4J and Logback
- **Testing:** JUnit 5 and Spring Boot Test
- **Version Control:** Git and GitHub

## Application Architecture

The application follows a layered architecture.

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
                   v
           Repository Layer
                   |
                   v
               Database
```

### Supporting Components

- **DTOs:** Transfer request and response data between the client and application.
- **Security Configuration:** Manages authentication and authorization.
- **JWT Components:** Handle token generation and validation.
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
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
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

*The exact package contents may grow as the application develops.*

## Prerequisites

Install the following before running the application:

- Java Development Kit (JDK) 21
- Apache Maven
- Git
- A compatible relational database
- An IDE such as IntelliJ IDEA, Eclipse, or VS Code

## Configuration

Before running the application, configure the database and other required environment-specific properties in:

```text
src/main/resources/application.properties
```

Do not commit actual database passwords, mail credentials, JWT secrets, or other sensitive values to the repository.

Configure the required environment variables before starting the application.

The password recovery functionality also requires the appropriate mail server configuration.

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

## Running Tests

Execute the complete test suite:

```bash
mvn clean test
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

Each feature is developed and tested independently before being integrated into the development branch.

## Part 1 Learning Outcomes

Through Part 1, the project covers the following Spring Boot backend concepts:

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

## Author

**Hemang**

**Repository:** https://github.com/Hemang71006/BL_FundooNotes_Backend