# Reminder API

All reminder endpoints require a valid JWT bearer token. A reminder can only be
created for a note owned by the authenticated user. The API accepts local
date-times interpreted in `Asia/Kolkata`; supplied times must be in the future.
Responses use the same local wall-clock convention and do not include a UTC
offset.

## Create

`POST /api/notes/{noteId}/reminders`

```json
{
  "reminderAt": "2026-12-01T10:30:00"
}
```

Returns `202 Accepted` with the existing text response. The reminder is stored
in MySQL before returning; retrieve its ID from the note reminder list.

## List reminders

- `GET /api/notes/reminders` — all reminders for the authenticated user.
- `GET /api/notes/reminders/upcoming` — pending reminders not yet due.
- `GET /api/notes/{noteId}/reminders` — reminders for an owned note.

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

## Manage

- `PUT /api/notes/reminders/{reminderId}` — change the future reminder time with
  the same JSON body as creation. A failed reminder can also be rescheduled.
- `PATCH /api/notes/reminders/{reminderId}/cancel` — cancel a pending or failed
  reminder.
- `PATCH /api/notes/reminders/{reminderId}/complete` — mark a pending or failed
  reminder complete without sending email.
- `DELETE /api/notes/reminders/{reminderId}` — permanently delete a reminder;
  returns `204 No Content`.

The management endpoints return the reminder response shown above, except
delete. A reminder belonging to another user is returned as `404 Not Found`.
Only `PENDING` and `FAILED` reminders may be updated, cancelled, or completed.

## Delivery and recovery

RabbitMQ messages contain only the persisted reminder ID. The consumer and the
MySQL-backed recovery scheduler use the same claim-and-delivery processor. A
message published when a future reminder is created may be consumed before it
is due; the database poller remains authoritative and recovers due reminders
after backend restarts or broker outages.

Email delivery uses the current note title and registered account email. Set
`MAIL_FROM` to a valid sender address. Configure `MAIL_HOST` and `MAIL_PORT` for
the SMTP server. If SMTP authentication is required, set `MAIL_USERNAME`,
`MAIL_PASSWORD`, `MAIL_SMTP_AUTH=true`, and, when supported,
`MAIL_SMTP_STARTTLS_ENABLE=true`. Authentication and STARTTLS default to
disabled for local unauthenticated mail servers; no sample credentials or
sender address are supplied. Without `MAIL_FROM`, a due reminder is not sent
and records `MAIL_FROM_NOT_CONFIGURED`.

Mail failures are retried up to five total attempts with exponential delays
starting at 30 seconds; exhausted reminders become `FAILED`.

Moving a note to trash cancels its pending/processing/failed reminders.
Restoring the note does not reactivate them. Permanently deleting a note
removes its reminders. Archiving does not cancel reminders.

Database claims carry a per-attempt fencing token. A worker whose claim was
replaced after its processing lease expired cannot send email, and concurrent
RabbitMQ redeliveries and recovery polling share the same atomic claim path.
SMTP itself does not provide transactional exactly-once delivery: if a mail
server accepts a message but the connection fails before the application
receives confirmation, a later retry can result in a duplicate.
