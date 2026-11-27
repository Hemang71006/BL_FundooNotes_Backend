# Reminder API

All reminder endpoints require a valid JWT bearer token. A reminder can only be
created for a note owned by the authenticated user. The API accepts local
date-times interpreted in `Asia/Kolkata`; supplied times must be in the future.
Responses use the same local wall-clock convention and do not include a UTC
offset.

## Time validation

`POST` and `PUT` bodies use the same request DTO:

```json
{
  "reminderAt": "2026-12-01T10:30:00"
}
```

Validation rules:

- `reminderAt` is required: `Reminder date and time are required`.
- `reminderAt` must be after `LocalDateTime.now(Asia/Kolkata)`:
  `Reminder date and time must be in the future`.
- The custom `@FutureKolkata` constraint delegates to
  `ReminderTime.now()`, so validation remains correct even when the JVM default
  timezone is not `Asia/Kolkata`.

## Create

`POST /api/notes/{noteId}/reminders`

```json
{
  "reminderAt": "2026-12-01T10:30:00"
}
```

Returns `202 Accepted` with the plain-text body:

```text
Reminder request accepted for processing
```

The reminder is stored in MySQL before returning. The response does **not**
contain the generated reminder ID; retrieve it from the note reminder list.
A foreign or missing note returns `404`, and a trashed note returns `400`.

## List reminders

- `GET /api/notes/reminders` — all reminders for the authenticated user.
- `GET /api/notes/reminders/upcoming` — `PENDING` reminders whose
  `reminderAt` is not before the current `Asia/Kolkata` time.
- `GET /api/notes/{noteId}/reminders` — reminders for an owned note; a foreign
  note returns `404`.

Each list item has this shape:

```json
{
  "id": 42,
  "noteId": 15,
  "noteTitle": "Study Java",
  "reminderAt": "2026-12-01T10:30:00",
  "status": "PENDING",
  "createdAt": "2026-11-27T13:00:00",
  "updatedAt": "2026-11-27T13:00:00",
  "deliveredAt": null,
  "retryCount": 0
}
```

Possible statuses are `PENDING`, `PROCESSING`, `SENT`, `FAILED`, `CANCELLED`, and
`COMPLETED`. Lists are ordered by `reminderAt`, then `createdAt`.

## Manage

- `PUT /api/notes/reminders/{reminderId}` — change the future reminder time with
  the same JSON body as creation. A failed reminder can also be rescheduled.
  A successful update sets the status back to `PENDING`, resets `retryCount` to
  `0`, clears claim/delivery fields, and publishes a new wake-up event.
- `PATCH /api/notes/reminders/{reminderId}/cancel` — cancel a pending or failed
  reminder.
- `PATCH /api/notes/reminders/{reminderId}/complete` — mark a pending or failed
  reminder complete without sending email.
- `DELETE /api/notes/reminders/{reminderId}` — permanently delete a reminder;
  returns `204 No Content`.

The management endpoints return the reminder response shown above, except
delete. A reminder belonging to another user is returned as `404 Not Found`.
Only `PENDING` and `FAILED` reminders may be updated, cancelled, or completed;
other states return `409 Conflict`.

## RabbitMQ wake-up

Topology comes from `application.properties`:

| Setting | Value |
|---|---|
| Exchange | `fundoo.notes.exchange` (durable direct exchange) |
| Queue | `fundoo.notes.reminder.queue` (durable) |
| Routing key | `reminder.requested` |
| Message converter | `JacksonJsonMessageConverter` |

On create and successful update, the application publishes this JSON after the
database commit:

```json
{
  "reminderId": 42,
  "eventType": "REMINDER_REQUESTED"
}
```

The consumer ignores messages with a missing or non-numeric `reminderId` and
uses only that ID. Publishing failures are logged and swallowed; the database
poller remains authoritative.

## Delivery and recovery

RabbitMQ messages contain only the persisted reminder ID. The consumer and the
MySQL-backed recovery scheduler use the same claim-and-delivery processor. A
message published when a future reminder is created may be consumed before it
is due; the database poller remains authoritative and recovers due reminders
after backend restarts or broker outages.

Recovery settings:

| Setting | Value |
|---|---|
| Poll interval | `app.reminder.poll-interval-ms=10000` (fixed delay) |
| Poll batch size | 100 due reminders |
| Maximum attempts | 5 total |
| Processing lease | 15 minutes before a stale `PROCESSING` claim can be reclaimed |
| Retry delays | 30 s, 60 s, 120 s, 240 s after failures 1–4 |
| Exhausted status | `FAILED` with `RETRIES_EXHAUSTED` |

Email delivery uses the current note title and registered account email. Set
`MAIL_FROM` to a valid sender address. Configure `MAIL_HOST` and `MAIL_PORT` for
the SMTP server. If SMTP authentication is required, set `MAIL_USERNAME`,
`MAIL_PASSWORD`, `MAIL_SMTP_AUTH=true`, and, when supported,
`MAIL_SMTP_STARTTLS_ENABLE=true`. Authentication and STARTTLS default to
disabled for local unauthenticated mail servers; no sample credentials or
sender address are supplied. Without `MAIL_FROM`, a due reminder is not sent
and records `MAIL_FROM_NOT_CONFIGURED`.

Before sending, the worker re-locks the note and reminder and checks a UUID
fencing token. A claim replaced after its lease expired cannot send email, and
RabbitMQ redeliveries and recovery polling share the same atomic claim path.

Moving a note to trash cancels its pending/processing/failed reminders.
Restoring the note does not reactivate them. Permanently deleting a note
removes its reminders. Archiving does not cancel reminders.

SMTP itself does not provide transactional exactly-once delivery: if a mail
server accepts a message but the connection fails before the application
receives confirmation, a later retry can result in a duplicate.

## Scope limits

The API accepts only `reminderAt`. There is no reminder message, repeat rule,
timezone field, or snooze endpoint. All times are local `Asia/Kolkata`
wall-clock values without an offset; clients must not append a UTC offset.
