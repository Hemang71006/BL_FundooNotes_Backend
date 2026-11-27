# Backend Feature Roadmap

This roadmap preserves the original sequence: **Feature 09 remains JMS messaging**, and the two later integrations are **Feature 16 (CORS)** and **Feature 17 (Reminders)**. Feature numbers are not duplicated or renumbered.

| Feature | Name | Branch | Integration intent |
|---|---|---|---|
| 01 | Database Configuration | `feature/01-database-configuration` | Core persistence |
| 02 | User Management | `feature/02-user-management` | Registration and user records |
| 03 | Authentication & Security | `feature/03-authentication-security` | JWT, BCrypt, protected routes |
| 04 | Note Management | `feature/04-note-management` | Note CRUD and state changes |
| 05 | Label & Search Management | `feature/05-label-search-management` | Labels, note-label links, search |
| 06 | Password Recovery | `feature/06-password-recovery` | Redis OTP and reset email |
| 07 | Global Exception Handling | `feature/07-global-exception-handling` | Consistent error responses |
| 08 | Logging & Debugging | `feature/08-logging-debugging` | Request interceptor and logging |
| 09 | JMS Messaging | `feature/09-jms-messaging` | User registration event after commit |
| 10 | RabbitMQ Integration | `feature/10-rabbitmq-integration` | Exchange, queue, binding, consumer |
| 11 | Redis Configuration & Integration | `feature/11-redis-integration` | Redis templates and temporary data |
| 12 | Caching Implementation | `feature/12-caching-implementation` | Redis-backed Spring Cache |
| 13 | Spring Batch & Excel Processing | `feature/13-spring-batch-excel` | XLSX import/export and job status |
| 14 | Aspect-Oriented Programming | `feature/14-aop-implementation` | Logging and timing advice |
| 15 | Advanced Architecture Integration | `feature/15-advanced-architecture-integration` | Cross-feature integration |
| 16 | CORS Configuration | `feature/16-cors-configuration` | Restrict browser origins to the Vite frontend |
| 17 | Reminder Integration | `feature/17-reminder-integration` | Owner-scoped reminders with RabbitMQ and DB recovery |

## Feature 16 — CORS Configuration

- Allowed origin: `http://localhost:5173` only.
- Allowed methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
- Allowed headers: `Authorization`, `Content-Type`, `Accept`.
- Credentials are not allowed.
- Public preflight requests use `OPTIONS /**`; all other non-public routes still require a valid JWT bearer token.
- Other origins, including `http://localhost:3000`, are rejected.

The CORS setting applies to the whole API and is intentionally not controlled by an environment property.

## Feature 17 — Reminder Integration

- Reminders are persisted in MySQL and scoped to both the authenticated user and an owned note.
- Clients submit only a local `reminderAt` value; validation uses `Asia/Kolkata` through the custom `@FutureKolkata` constraint and `ReminderTime.now()`, so validation does not depend on the JVM default timezone.
- Creation returns `202 Accepted` and a plain-text message; it does not return the generated reminder ID. Clients read the ID from the note reminder list.
- RabbitMQ publishes `{"reminderId": <id>, "eventType": "REMINDER_REQUESTED"}` as JSON after the database commit. The consumer and MySQL-backed recovery scheduler share the same claim-and-delivery path.
- Recovery uses a configurable `app.reminder.poll-interval-ms=10000` poll, a 15-minute processing lease, up to five total attempts, and 30/60/120/240-second exponential retry delays.
- Trashing a note cancels its pending, processing, and failed reminders; restoring the note does not reactivate them. Archiving does not cancel reminders.
- SMTP acceptance and the database status update are not atomic; duplicate email delivery remains possible after an ambiguous SMTP failure.

See [docs/reminders.md](docs/reminders.md) for the full reminder contract and limitations.

## Cross-feature verification

The integrated branch was verified with `mvn -B clean verify`:

- **142 tests**
- **0 failures**
- **0 errors**
- **0 skips**
- `git diff --check` reported no whitespace errors.

Focused verification covered Redis immediate writes and Java-time serialization, RabbitMQ JSON round-trip through a real broker, JMS registration flow, batch import/export ownership and cleanup, reminder lifecycle/security, CORS/JWT, notes/labels/search, password recovery, exception handling, and AOP advice behavior.
