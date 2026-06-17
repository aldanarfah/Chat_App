# Architecture

## Current State

The current foundation is a Maven Java app that:
- Starts an HTTP server on port `8080`
- Serves static frontend assets from `src/main/resources/public`
- Starts a WebSocket server on port `8887`
- Exposes REST APIs for sessions, rooms, and active users
- Routes structured JSON WebSocket chat events with in-memory state

Current code layout:
- `app.Application`: application startup and wiring
- `app.http`: static file serving
- `app.file`: local file upload validation and storage
- `app.websocket`: WebSocket server and connection helpers

The frontend is still plain HTML/CSS/JS and currently remains a simple demo chat UI.

Network note:
- The app is intended to be reachable by other devices on the same VPN/LAN.
- Run the host server on `0.0.0.0` or the VPN interface IP.
- Other users should open the app with the host's VPN/LAN IP, not `localhost`.

## Target Foundation

Move to a Maven-based Java project while keeping the app lightweight.

Target responsibilities:
- HTTP server for static files and REST APIs
- WebSocket server for realtime events
- In-memory services for sessions, rooms, DMs, and messages
- Local file storage under `uploads/`
- Plain frontend assets served from `src/main/resources/public`

## Backend Modules

Recommended package structure:

```text
app
app.http
app.websocket
app.session
app.chat
app.file
app.model
app.util
```

Recommended responsibilities:
- `app`: application startup and server wiring
- `app.http`: REST route handlers and static file serving
- `app.websocket`: WebSocket connection handling and event dispatch
- `app.session`: session creation, active users, connection mapping
- `app.chat`: rooms, memberships, room messages, direct messages
- `app.file`: upload validation, local storage, download lookup
- `app.model`: event/message/session/file data records
- `app.util`: JSON, IDs, time, filename safety helpers

## REST API

Target MVP endpoints:

```text
POST /api/session
GET  /api/rooms
POST /api/rooms
GET  /api/users
POST /api/files
GET  /api/files/{fileId}
```

Expected behavior:
- `POST /api/session` creates a session from a display name.
- `POST /api/session` may receive optional `forceTakeover: true` to reclaim an active display name from another device.
- `GET /api/rooms` returns available rooms.
- `POST /api/rooms` creates a room.
- `GET /api/users` returns active users.
- `POST /api/files` uploads one multipart file field named `file` and returns metadata.
- `GET /api/files/{fileId}` downloads a stored file.

Current implementation status:
- Implemented now: `POST /api/session`, `GET /api/rooms`, `POST /api/rooms`, `GET /api/users`, `POST /api/files`, `GET /api/files/{fileId}`

Session creation conflict shape:

```json
{
  "error": {
    "code": "username_in_use",
    "message": "Nama ini sedang aktif di perangkat lain. Ambil alih sesi untuk masuk kembali.",
    "canTakeOver": true
  }
}
```

Upload response shape:

```json
{
  "fileId": "file_123",
  "filename": "laporan.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 48291,
  "downloadUrl": "/api/files/file_123",
  "uploadedAt": "2026-06-03T12:00:00"
}
```

## WebSocket API

Target endpoint:

```text
ws://localhost:8887/ws?sessionId=...
```

All WebSocket messages are JSON, not raw strings.

Implemented client event types:

```text
room.message.send
dm.message.send
room.join
room.leave
```

Implemented server event types:

```text
session.ready
presence.joined
presence.left
room.joined
room.left
room.membership.updated
room.message.receive
dm.message.receive
error
```

Additional `error` event code used by the client:
- `session_taken_over`: the same display name was reclaimed from another device and the old socket should return to the login view.

Recommended message shape:

```json
{
  "type": "room.message.send",
  "payload": {
    "conversationId": "room-general",
    "text": "Halo semua",
    "fileId": "file_123"
  }
}
```

File sharing uses the existing chat send events:
- `room.message.send` for room file shares
- `dm.message.send` for DM file shares
- `text` is optional when `fileId` is present
- at least one of `text` or `fileId` must be present

Recommended server message shape:

```json
{
  "type": "room.message.receive",
  "payload": {
    "id": "msg_123",
    "conversationId": "room-general",
    "conversationType": "room",
    "senderSessionId": "ses_123",
    "senderName": "Budi",
    "text": "Halo semua",
    "fileId": "file_123",
    "file": {
      "fileId": "file_123",
      "filename": "laporan.pdf",
      "contentType": "application/pdf",
      "sizeBytes": 48291,
      "downloadUrl": "/api/files/file_123",
      "uploadedAt": "2026-06-03T12:00:00"
    },
    "createdAt": "2026-06-03T12:00:00"
  }
}
```

Session bootstrap shape:

```json
{
  "type": "session.ready",
  "payload": {
    "session": {
      "sessionId": "ses_123",
      "username": "Budi",
      "active": true
    },
    "activeUsers": [],
    "rooms": []
  }
}
```

Room membership update shape:

```json
{
  "type": "room.membership.updated",
  "payload": {
    "room": {
      "roomId": "room-general",
      "name": "Umum"
    },
    "members": []
  }
}
```

## In-Memory State

MVP state should include:
- Sessions by `sessionId`
- Active WebSocket connections by `sessionId`
- Rooms by `roomId`
- Room memberships
- Room messages by `roomId`
- DM messages by conversation key
- File metadata by `fileId`

This state is intentionally lost on server restart.

## File Storage

MVP file rules:
- Store file bytes under `uploads/`
- Generate server-side safe filenames
- Keep original filename in metadata for display
- Max file size: `10 MB`
- Allow: images, PDF, TXT, ZIP
- Reject unsupported file types and unsafe names
- Sanitize user-provided filenames before display and download headers

## Error Handling

Errors should return structured JSON for REST and `error` events for WebSocket.

User-facing messages should be understandable in Indonesian. Internal logs may use English.
