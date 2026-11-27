# Fundoo Notes Backend

A Java 21 / Spring Boot backend for a note-taking application inspired by Google Keep. The project is developed feature by feature using Git branches, with each feature implemented, tested, and integrated into `develop`.

## Current state

The active work is Feature 17, **Reminder Integration**, on `feature/17-reminder-integration`. Features 09–17 remain intact:

| Feature | Name | Branch |
|---|---|---|
| 01 | Database Configuration | `feature/01-database-configuration` |
| 02 | User Management | `feature/02-user-management` |
| 03 | Authentication & Security | `feature/03-authentication-security` |
| 04 | Note Management | `feature/04-note-management` |
| 05 | Label & Search Management | `feature/05-label-search-management` |
| 06 | Password Recovery | `feature/06-password-recovery` |
| 07 | Global Exception Handling | `feature/07-global-exception-handling` |
| 08 | Logging & Debugging | `feature/08-logging-debugging` |
| 09 | JMS Messaging | `feature/09-jms-messaging` |
| 10 | RabbitMQ Integration | `feature/10-rabbitmq-integration` |
| 11 | Redis Configuration & Integration | `feature/11-redis-integration` |
| 12 | Caching Implementation | `feature/12-caching-implementation` |
| 13 | Spring Batch & Excel Processing | `feature/13-spring-batch-excel` |
| 14 | Aspect-Oriented Programming | `feature/14-aop-implementation` |
| 15 | Advanced Architecture Integration | `feature/15-advanced-architecture-integration` |
| 16 | CORS Configuration | `feature/16-cors-configuration` |
| 17 | Reminder Integration | `feature/17-reminder-integration` |

See [docs/feature-roadmap.md](docs/feature-roadmap.md) for the feature sequence and integration notes.

## What the API supports

- User registration and JWT login/logout.
- Password recovery with a six-digit OTP stored in Redis and delivered by email.
- Note create, read, update, delete, pin, unpin, archive, unarchive, trash, and restore.
- Label create, update, delete, and attachment to notes.
- Note search by keyword, label, and status.
- Owner-scoped reminders with RabbitMQ wake-ups, MySQL-backed recovery, and retry/fencing logic.
- Excel import/export with Spring Batch.
- Redis caching with immediate writes and ten-minute TTL.
- JMS user-registration events with an embedded ActiveMQ broker.
- CORS limited to the configured React frontend origin `http://localhost:5173`.
- Centralized validation, exception responses, request logging, and AOP timing/logging.

Detailed contracts for frontend consumers are in [docs/frontend-api-integration.md](docs/frontend-api-integration.md). Reminder behavior is documented in [docs/reminders.md](docs/reminders.md).

## Architecture

```text
Client (http://localhost:5173)
        |
        v
REST Controllers + DTO validation
        |
        v
Service layer
   /    |     \      \
Redis  JPA   JMS   RabbitMQ
        |
       MySQL
```

Supporting components:

- `config/` — security, CORS, Redis, RabbitMQ, JMS, scheduling, interceptors.
- `controller/` — REST endpoints.
- `dto/request/` and `dto/response/` — validated request contracts and response shapes.
- `service/` and `service/impl/` — business logic.
- `repository/` — Spring Data JPA repositories.
- `security/` — JWT generation, validation, and request filtering.
- `messaging/` — JMS and RabbitMQ producers/consumers.
- `cache/` and `redis/` — cache configuration and Redis-backed OTP/blacklist operations.
- `batch/` — Excel import/export jobs.
- `aspect/` — cross-cutting logging and execution-time advice.
- `exception/` — global error responses.
- `validation/` — custom `Asia/Kolkata` reminder-time validation.

## Technology stack

| Category | Technologies |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build | Maven |
| Database | MySQL, Spring Data JPA, Hibernate |
| Security | Spring Security, JWT, BCrypt |
| Validation | Jakarta Bean Validation |
| Messaging | JMS/ActiveMQ, RabbitMQ |
| Cache / temporary data | Redis, Spring Cache |
| Batch / Excel | Spring Batch, Apache POI |
| Cross-cutting | Spring AOP, SLF4J/Logback |
| Testing | JUnit 5, Mockito, Spring Boot Test |

## Prerequisites

- JDK 21
- Maven
- MySQL
- Redis
- RabbitMQ
- An SMTP server for password-reset and reminder email (a local unauthenticated server works for development)
- Git

ActiveMQ is supplied through the embedded broker dependency; no separate broker URL is required by default.

## Configuration

Application settings live in `src/main/resources/application.properties`. Sensitive values should be supplied through environment variables.

| Variable | Purpose | Default / note |
|---|---|---|
| `JWT_SECRET` | JWT signing secret | **Required**, no default |
| `DB_USERNAME`, `DB_PASSWORD` | MySQL credentials | Defaults: `root` and a required password; JDBC URL is set in `application.properties` |
| `REDIS_HOST`, `REDIS_PORT` | Redis connection | `localhost`, `6379` |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | RabbitMQ connection | `localhost`, `5672`, `guest`, `guest` |
| `MAIL_HOST`, `MAIL_PORT` | SMTP server | `localhost`, `1025` |
| `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS_ENABLE` | SMTP authentication | Empty / false by default |
| `MAIL_FROM` | Sender address for welcome, reset, and reminder mail | Empty by default; must be set to send mail |
| `PASSWORD_RESET_URL` | Frontend origin included in reset OTP email text | `http://localhost:5173` (origin only; no route is assumed) |
| `app.reminder.poll-interval-ms` | Reminder recovery poll interval | `10000` ms |

CORS is intentionally fixed to `http://localhost:5173`; it is not property-configurable.

## Build and run

```bash
mvn clean install
mvn spring-boot:run
```

The API listens on `http://localhost:8080` by default.

## Tests

```bash
mvn clean verify
```

The verified build ran **142 tests** with **0 failures, 0 errors, and 0 skips**, and `git diff --check` reported no whitespace errors. Focused suites also covered Redis immediate writes and serialization, RabbitMQ JSON round-trip, JMS registration, batch import/export security and cleanup, reminder lifecycle and security, CORS/JWT, notes/labels/search, password recovery, exception handling, and AOP advice.

## API quick reference

All protected endpoints require:

```http
Authorization: Bearer <JWT>
```

Public endpoints:

- `POST /api/users/register`
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `OPTIONS /**` for CORS preflight

Main route groups:

```text
/api/auth/login, /api/auth/logout, /api/auth/forgot-password, /api/auth/reset-password
/api/users/register
/api/notes
/api/notes/archived, /api/notes/trash, /api/notes/search
/api/notes/{noteId}, /api/notes/{noteId}/pin, /api/notes/{noteId}/unpin
/api/notes/{noteId}/archive, /api/notes/{noteId}/unarchive
/api/notes/{noteId}/trash, /api/notes/{noteId}/restore
/api/labels, /api/notes/{noteId}/labels/{labelId}
/api/notes/{noteId}/reminders, /api/notes/reminders, /api/notes/reminders/upcoming
/api/notes/reminders/{reminderId}, /api/notes/reminders/{reminderId}/cancel
/api/notes/reminders/{reminderId}/complete
/api/batch/import, /api/batch/status/{executionId}, /api/batch/export
```

Error responses use a consistent JSON body:

```json
{
  "timestamp": "2026-11-28T10:15:30.123456Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/notes",
  "validationErrors": {
    "title": "Note title is required"
  }
}
```

Authentication failures are emitted by Spring Security as plain `401 Unauthorized` responses.

## Git workflow

- `main` — original project baseline; not modified by this feature work.
- `develop` — integration branch.
- `feature/*` — one branch per feature.

Feature commits use the prefix `[Hemang]: `. No force pushes are used.

## Author

**Hemang**

Repository: https://github.com/Hemang71006/BL_FundooNotes_Backend
