# Backend Feature Roadmap

The backend feature sequence preserves Feature 09 for JMS and numbers the two
subsequent integrations as Features 16 and 17.

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

Feature 16 enables the React frontend at `http://localhost:5173` to call the
backend without weakening JWT-protected routes. Feature 17 provides
owner-scoped, persisted note reminders with RabbitMQ wake-ups and database
recovery.
