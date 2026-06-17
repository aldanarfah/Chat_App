# Frontend Context

## Current Frontend

The current frontend is:
- `src/main/resources/public/index.html`
- `src/main/resources/public/app.js`
- Inline CSS inside `index.html`
- One centered demo panel
- Raw WebSocket string messages

The UI implementation phase should replace this with a full chat app shell.

## Target Files

Recommended MVP frontend files:

```text
src/main/resources/public/index.html
src/main/resources/public/styles.css
src/main/resources/public/app.js
```

Keep the frontend plain HTML/CSS/JS for MVP. Do not add a frontend build tool unless a later plan explicitly changes direction.

## App State

Recommended JS state shape:

```js
const state = {
  session: null,
  socket: null,
  connected: false,
  activeConversationId: null,
  activeConversationType: null,
  rooms: [],
  users: [],
  conversations: {},
  messagesByConversation: {},
  uploads: {}
};
```

Conversation types:
- `room`
- `dm`

## Frontend Responsibilities

The frontend should:
- Create a session from a display name
- Open a WebSocket using `sessionId`
- Fetch rooms and users
- Render room and DM conversation lists
- Render messages per active conversation
- Send room messages
- Send DM messages
- Upload files through REST
- Send file message events over WebSocket
- Render file cards and download links
- Handle disconnected and error states

## Render Regions

Recommended DOM regions:
- `appShell`
- `sessionView`
- `iconRail`
- `conversationSidebar`
- `conversationList`
- `chatPanel`
- `chatHeader`
- `messageTimeline`
- `composer`
- `toastRegion`

## UI Copy

Use Indonesian UI copy.

Recommended strings:
- `Masuk ke Chat`
- `Nama tampilan`
- `Mulai`
- `Chat`
- `Semua`
- `Grup`
- `Orang`
- `Pilih percakapan`
- `Belum ada pesan`
- `Ketik pesan...`
- `Kirim`
- `Lampirkan file`
- `Mengunggah file...`
- `File berhasil dikirim`
- `File terlalu besar`
- `Format file tidak didukung`
- `Koneksi terputus`
- `Mencoba terhubung...`

## Event Flow

Session flow:
1. User enters display name.
2. Frontend calls `POST /api/session`.
3. If the name is still active elsewhere, frontend may offer to retry with `forceTakeover: true`.
4. Server returns session metadata.
5. Frontend opens WebSocket with `sessionId`.
6. Frontend fetches rooms and active users.

Room message flow:
1. User selects a room.
2. User types text and sends.
3. Frontend emits `room.message.send`.
4. Server validates membership and broadcasts `room.message.receive`.
5. Frontend appends received message to the conversation.

DM flow:
1. User selects another active user.
2. Frontend creates/selects a DM conversation.
3. User sends message.
4. Frontend emits `dm.message.send`.
5. Server sends `dm.message.receive` to sender and target.

File flow:
1. User chooses a file.
2. Frontend uploads it to `POST /api/files`.
3. Server returns file metadata.
4. Frontend emits `room.message.send` or `dm.message.send` with `fileId`.
5. Server broadcasts/sends the normal room/DM receive event with embedded `file` metadata.
6. Frontend renders a file card using the returned `downloadUrl`.

## Responsive Behavior

Desktop:
- Show rail, sidebar, and active chat at once.

Mobile:
- Show conversation list by default.
- Selecting a conversation switches to active chat.
- Chat header includes a back button.
- Composer stays visible at bottom.

## Implementation Notes

- Keep rendering functions small and explicit.
- Avoid global DOM rewrites for every small event when possible.
- Escape user-provided text before rendering into HTML.
- Prefer `textContent` for message text.
- Use `URL.createObjectURL` only for local previews, not server file downloads.
- Keep file download URLs server-provided.
