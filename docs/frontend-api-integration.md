# Frontend API Integration Guide

This document describes the **actual** HTTP contracts exposed by this backend. It is intended for the React frontend running at `http://localhost:5173`. Examples use the default backend origin `http://localhost:8080`.

No OpenAPI/Swagger definition is published, so treat this document and the controller/DTO sources as the contract.

## 1. Connection and CORS

| Setting | Value |
|---|---|
| Backend base URL | `http://localhost:8080` by default |
| Allowed browser origin | `http://localhost:5173` only |
| Allowed methods | `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS` |
| Allowed headers | `Authorization`, `Content-Type`, `Accept` |
| Credentials | Not allowed (`Access-Control-Allow-Credentials` is not sent) |

Requests from any other origin, including `http://localhost:3000`, fail CORS preflight. CORS is hard-coded and is not configurable through an environment variable.

## 2. Authentication

### 2.1 Protected requests

Every protected endpoint requires this exact header:

```http
Authorization: Bearer <JWT>
```

The prefix is case-sensitive and must be followed by a single space. There are no roles or authorities; ownership is checked from the JWT subject (the user email).

Public routes:

- `POST /api/users/register`
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `OPTIONS /**`

Everything else, including `POST /api/auth/logout`, requires a valid JWT.

JWT details:

- `jwt.secret` comes from the required `JWT_SECRET` environment variable; there is no default.
- Expiration is 86,400,000 ms (24 hours).
- There is no refresh token.
- Logout blacklists the SHA-256 hash of the token in Redis for the remaining token lifetime and requires the same `Authorization: Bearer <JWT>` header.

### 2.2 Register

`POST /api/users/register` — public

```json
{
  "firstName": "Hemang",
  "lastName": "Shah",
  "email": "hemang@example.com",
  "password": "MyPassword123"
}
```

`201 Created`:

```json
{
  "id": 1,
  "firstName": "Hemang",
  "lastName": "Shah",
  "email": "hemang@example.com",
  "createdAt": "2026-11-28T10:00:00"
}
```

Validation: first/last name required and at most 50 characters; email must be valid and at most 100 characters; password must be 8–100 characters. A duplicate email returns `409` with message `Email is already registered`.

After commit, the backend publishes a JMS user-registration event and attempts to send a welcome email.

### 2.3 Login

`POST /api/auth/login` — public

```json
{
  "email": "hemang@example.com",
  "password": "MyPassword123"
}
```

`200 OK`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "email": "hemang@example.com",
  "message": "Login successful"
}
```

Invalid credentials return `401` with `Invalid email or password`.

### 2.4 Logout

`POST /api/auth/logout` — protected, no body

```http
Authorization: Bearer <JWT>
```

`204 No Content` with an empty body.

### 2.5 Password recovery

`POST /api/auth/forgot-password` — public

```json
{
  "email": "hemang@example.com"
}
```

`200 OK`, plain text:

```text
If the email is registered, a password reset OTP has been sent.
```

The backend always returns the same message whether or not the email exists. It generates a six-digit OTP, stores it in Redis for 15 minutes, and emails it.

`POST /api/auth/reset-password` — public

```json
{
  "email": "hemang@example.com",
  "otp": "482913",
  "newPassword": "NewPassword123"
}
```

`200 OK`, plain text:

```text
Password has been reset successfully
```

Invalid, expired, or mismatched OTPs return `400`. The OTP is deleted after a successful database commit.

The reset email includes the configured `app.password-reset.url` as an origin only:

- Default: `http://localhost:5173`
- Override: `PASSWORD_RESET_URL` environment variable

The backend does **not** define a frontend reset route. The frontend must implement its own screen and then call `POST /api/auth/reset-password` with `email`, `otp`, and `newPassword`. Do not rely on a backend-prescribed path such as `/reset-password`.

## 3. DTO reference

### 3.1 Request fields

| DTO | Field | Type | Rules |
|---|---|---|---|
| `RegisterRequest` | `firstName` | string | Required, max 50 |
| | `lastName` | string | Required, max 50 |
| | `email` | string | Required, valid email, max 100 |
| | `password` | string | Required, 8–100 |
| `LoginRequest` | `email` | string | Required, valid email |
| | `password` | string | Required |
| `ForgotPasswordRequest` | `email` | string | Required, valid email |
| `ResetPasswordRequest` | `email` | string | Required, valid email |
| | `otp` | string | Required, exactly six digits |
| | `newPassword` | string | Required, min 8; no max constraint in DTO |
| `NoteRequest` | `title` | string | Required, max 255 |
| | `description` | string | Optional, max 5000 |
| `LabelRequest` | `name` | string | Required, max 50 |
| | `color` | string | Optional, max 7; no hex pattern check |
| `ReminderRequest` | `reminderAt` | local date-time | Required; must be future in `Asia/Kolkata` |

### 3.2 Response fields

| DTO | Fields |
|---|---|
| `UserResponse` | `id`, `firstName`, `lastName`, `email`, `createdAt` |
| `LoginResponse` | `token`, `tokenType`, `email`, `message` |
| `NoteResponse` | `id`, `title`, `description`, `pinned`, `archived`, `trashed`, `createdAt`, `updatedAt` |
| `LabelResponse` | `id`, `name`, `color` |
| `ReminderResponse` | `id`, `noteId`, `noteTitle`, `reminderAt`, `status`, `createdAt`, `updatedAt`, `deliveredAt`, `retryCount` |
| `ErrorResponse` | `timestamp`, `status`, `error`, `message`, `path`, `validationErrors` |

`NoteResponse` does **not** include labels or `userId`. Use the label association endpoints if the frontend needs label data.

## 4. Notes

Base path: `/api/notes`. All routes are protected.

### 4.1 Create and read

`POST /api/notes`

```json
{
  "title": "Study Java",
  "description": "Revision notes for Spring Boot"
}
```

`201 Created`:

```json
{
  "id": 15,
  "title": "Study Java",
  "description": "Revision notes for Spring Boot",
  "pinned": false,
  "archived": false,
  "trashed": false,
  "createdAt": "2026-11-27T13:00:00",
  "updatedAt": "2026-11-27T13:00:00"
}
```

Read endpoints:

- `GET /api/notes` — active notes (`archived=false`, `trashed=false`), ordered by `updatedAt desc`.
- `GET /api/notes/archived` — archived, non-trashed notes.
- `GET /api/notes/trash` — trashed notes, including archived ones.
- `GET /api/notes/{noteId}` — one owned note; `404` if not owned.

All return either a single `NoteResponse` or an array of `NoteResponse`.

### 4.2 Update and delete

`PUT /api/notes/{noteId}` is a **full replacement**, not a patch:

```json
{
  "title": "Study Java 21",
  "description": "Updated revision notes"
}
```

Omitting `description` sets it to `null`. Updating a trashed note returns `400`.

`DELETE /api/notes/{noteId}` permanently deletes the note and its owned reminders; returns `204 No Content`.

### 4.3 State changes

These endpoints take no body and return a full `NoteResponse`:

```text
PATCH /api/notes/{noteId}/pin
PATCH /api/notes/{noteId}/unpin
PATCH /api/notes/{noteId}/archive
PATCH /api/notes/{noteId}/unarchive
PATCH /api/notes/{noteId}/trash
PATCH /api/notes/{noteId}/restore
```

Rules:

- A trashed note cannot be updated, pinned, unpinned, archived, unarchived, or labelled. `restore` and permanent `delete` remain available.
- `trash` cancels the note's `PENDING`, `PROCESSING`, and `FAILED` reminders.
- `restore` does not reactivate cancelled reminders.
- `archive` does not cancel reminders.

## 5. Labels

Base path: `/api/labels`.

- `POST /api/labels` — create; `201` with `LabelResponse`.
- `GET /api/labels` — list all labels for the user, ordered by name.
- `PUT /api/labels/{labelId}` — update; `200` with `LabelResponse`.
- `DELETE /api/labels/{labelId}` — delete; `204`. Removing a label also detaches it from all owned notes.

Create/update body:

```json
{
  "name": "Work",
  "color": "#FF5722"
}
```

Duplicate names are case-insensitive per user and return `409` with `Label already exists`.

Attach/detach:

```text
POST   /api/notes/{noteId}/labels/{labelId}    -> 200 with the attached LabelResponse
DELETE /api/notes/{noteId}/labels/{labelId}    -> 204 No Content
```

Both require ownership. Adding a label to a trashed note returns `400`.

## 6. Search

`GET /api/notes/search` — protected

Query parameters:

| Parameter | Meaning |
|---|---|
| `keyword` | Optional case-insensitive keyword match |
| `labelId` | Optional owned label ID; a foreign label returns `404` |
| `status` | Optional, case-insensitive: `ACTIVE`, `PINNED`, `ARCHIVED`, `TRASHED`; defaults to `ACTIVE` |

Examples:

```text
GET /api/notes/search?keyword=java
GET /api/notes/search?keyword=java&status=ARCHIVED
GET /api/notes/search?labelId=3&status=PINNED
GET /api/notes/search?status=TRASHED
```

Response: `200` with a plain array of `NoteResponse`. An invalid `status` returns `400`.

There are **no** pagination parameters (`page`, `size`, `pageNo`, etc.) and **no** client-controlled sort parameter. Ordering is fixed by the server (`updatedAt desc` for notes/search, `reminderAt` then `createdAt` for reminders).

## 7. Reminders

Reminder routes are nested under `/api/notes`; there is no `/api/reminders` base path. See [docs/reminders.md](docs/reminders.md) for full lifecycle and delivery details.

```text
POST   /api/notes/{noteId}/reminders
GET    /api/notes/reminders
GET    /api/notes/reminders/upcoming
GET    /api/notes/{noteId}/reminders
PUT    /api/notes/reminders/{reminderId}
PATCH  /api/notes/reminders/{reminderId}/cancel
PATCH  /api/notes/reminders/{reminderId}/complete
DELETE /api/notes/reminders/{reminderId}
```

Create/update body:

```json
{
  "reminderAt": "2026-12-01T10:30:00"
}
```

Important integration behavior:

- Times are local `Asia/Kolkata` wall-clock values without UTC offsets.
- Validation is independent of the JVM default timezone and uses the error message `Reminder date and time must be in the future`.
- Create returns `202 Accepted` and plain text, **not** the generated reminder ID.
- Read the ID from `GET /api/notes/{noteId}/reminders`.
- Only `PENDING` and `FAILED` reminders can be updated, cancelled, or completed.
- Other states return `409`; foreign reminders return `404`.
- Delete returns `204`.

## 8. Batch import and export

### 8.1 Import

`POST /api/batch/import` — protected, `multipart/form-data`, part name **`file`**.

Restrictions:

- Only `.xlsx` filenames are accepted.
- Empty files return `400`.
- Files larger than 1 MiB return `413` with `Excel files must not exceed 1 MiB`.
- Spring multipart limits are 1 MB per file and 2 MB per request.
- Ownership always comes from the JWT; the spreadsheet's `userId` column is parsed but ignored.

`202 Accepted`:

```json
{
  "message": "Excel import job started",
  "jobExecutionId": 12,
  "status": "STARTING"
}
```

Validation errors use a JSON body such as:

```json
{ "message": "Only .xlsx files are supported" }
```

The uploaded file is written to a temporary file, deleted if job startup fails, and removed again by a job listener after the job finishes.

### 8.2 Job status

`GET /api/batch/status/{executionId}` — protected

```json
{
  "jobExecutionId": 12,
  "status": "COMPLETED",
  "exitStatus": "COMPLETED"
}
```

A missing execution **or** an execution started by another user returns `404`, not `403`.

### 8.3 Export

`GET /api/batch/export` — protected. `GET /api/batch/export/{userId}` is also accepted only when `userId` equals the authenticated user's ID; otherwise it returns `404`.

Response headers:

```http
Content-Disposition: attachment; filename=fundoo-notes.xlsx
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

The response body is raw XLSX bytes, not JSON. The workbook has a sheet named `Notes` with this header row:

```text
id | title | description | pinned | archived | trashed | createdAt | updatedAt
```

Import expects this positional six-column layout and skips row 0 as a header:

```text
userId | title | description | pinned | archived | trashed
```

Consequences:

- Export output is not a lossless import input; `id` and timestamps are ignored/offset by one position.
- Boolean cells accept blank/`false` as false and `true` case-insensitively; other values skip the row.
- Invalid rows are skipped; the job skips up to 100 `IllegalArgumentException`s in chunks of 100.
- Title must be nonblank and at most 255 characters; description at most 5000 characters.

## 9. Error responses

Validation and application errors use this JSON shape:

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

Common mappings:

| Condition | HTTP status |
|---|---|
| Bean validation failure | 400 |
| Missing/invalid JSON body | 400 |
| Invalid reminder time | 400 with `validationErrors.reminderAt` |
| Unauthenticated request | 401, emitted by Spring Security as plain `Unauthorized` |
| Forbidden CORS origin preflight | 403 |
| Foreign/missing resource | 404 |
| HTTP method not allowed | 405 |
| Duplicate user/label or data conflict | 409 |
| Reminder state transition not allowed | 409 |
| Unexpected server error | 500 |

Do not assume every successful response is JSON:

- Forgot-password, reset-password, and reminder-create return plain text.
- Logout, delete, and detach-label return `204` with no body.
- Batch export returns binary XLSX.
- DTO endpoints return JSON.

## 10. Frontend integration gaps and cautions

1. **No labels inside notes.** `NoteResponse` has no `labels` field. Maintain label state through `/api/labels` and `/api/notes/{noteId}/labels/{labelId}`.
2. **Reminder create does not return an ID.** Fetch reminders by note after creation.
3. **Route literals matter.** Use `/api/notes/reminders` and `/api/notes/search` exactly; reminder routes are not under `/api/reminders`.
4. **No pagination or sorting controls.** Lists are complete and server-ordered.
5. **Search parameter names are exact.** Use `keyword`, `labelId`, and `status`; invalid `status` values return `400`.
6. **PUT replaces notes.** Send both `title` and `description` on update.
7. **Trashed notes are read-only except restore/delete.** Hide update/status/label actions for trashed notes.
8. **Batch export/import are not symmetrical.** The spreadsheet column layouts differ.
9. **Batch is not an admin feature.** Another user's `userId` or job execution yields `404`.
10. **Password reset is OTP-based, not a URL-token flow.** `PASSWORD_RESET_URL` is only prose in the email; the frontend owns the reset screen and posts `email` + `otp` + `newPassword`.
11. **CORS is fixed to `http://localhost:5173` with credentials disabled.** Do not configure a different dev port without changing the backend.
12. **Mail is optional at runtime.** With an empty `MAIL_FROM`, welcome mail, OTP mail, and reminder mail cannot be sent; reminders record `MAIL_FROM_NOT_CONFIGURED`.
13. **No user profile endpoints.** Only registration exists under `/api/users`.
14. **No OpenAPI, API versioning beyond `/api`, or request-ID header.** Internal logs are available but are not part of the HTTP contract.
15. **Validation differences:** `newPassword` has a minimum but no maximum in its DTO; `color` allows any value of at most 7 characters rather than enforcing a hex pattern.

## 11. Suggested client flow

```text
1. POST /api/users/register
2. POST /api/auth/login -> store token
3. Send Authorization: Bearer <token> on every protected request
4. Use /api/notes and /api/labels for note/label CRUD
5. For reminders, POST then GET /api/notes/{noteId}/reminders to obtain the ID
6. For batch work, POST multipart file, poll /api/batch/status/{executionId}, then GET /api/batch/export
7. POST /api/auth/logout with the same token when the session ends
```
