# Workflow

## Recommended Chat Strategy

Use multiple focused Codex chats after this docs/context setup. The docs are the shared memory. Each new chat should read `AGENTS.md` and the relevant docs before changing code.

Do not build the whole app in one massive pass. Use feature slices.

## Standard New Chat Opener

```text
Please read AGENTS.md first, then the relevant docs for this task. Follow the project workflow and do not assume old chat context unless it is written in the repo docs.

Task:
[describe the specific phase]

After implementation, run the relevant checks and update docs if behavior changed.
```

## Phase Prompts

### 1. Backend Foundation

```text
Read AGENTS.md and docs/ARCHITECTURE.md first.

Convert this project from the current run.sh/manual jar setup into a Maven Java app.

Keep the existing simple chat behavior working first:
- serve the frontend
- start HTTP server
- start WebSocket server
- allow browser clients to send/receive messages

Do not add rooms, DMs, or file upload yet. Keep this as a clean foundation slice. Update README/run instructions and run the build/test checks.
```

### 2. Rooms And DMs

```text
Read AGENTS.md, docs/PRD.md, and docs/ARCHITECTURE.md.

Implement the MVP chat model:
- username session creation
- active users
- rooms
- room membership
- room messages
- direct messages
- structured JSON WebSocket events

Keep storage in memory only. Add focused backend tests for routing behavior. Update docs if the event names or endpoints differ from the plan.
```

### 3. File Sharing

```text
Read AGENTS.md, docs/PRD.md, and docs/ARCHITECTURE.md.

Implement local file sharing:
- POST /api/files upload endpoint
- GET /api/files/{fileId} download endpoint
- store files under uploads/
- max file size 10 MB
- allow images, PDF, TXT, ZIP
- sanitize filenames
- return file metadata for chat messages
- support file message events in rooms and DMs

Add tests for file validation and filename safety. Update docs as needed.
```

### 4. UI Implementation

```text
Read AGENTS.md, docs/DESIGN_SYSTEM.md, and docs/FRONTEND_CONTEXT.md.

Replace the current simple centered WebSocket demo UI with the full responsive chat dashboard.

Requirements:
- Indonesian UI copy
- dark serious app style inspired by the reference image, not an exact clone
- left icon rail
- chat list sidebar
- room/DM filters
- active chat header
- message timeline
- outgoing/incoming bubbles
- file message cards
- attachment button
- send button
- typing/presence states
- responsive mobile behavior with list view and active chat view

Keep plain HTML/CSS/JS for MVP. Split CSS into styles.css if appropriate. Connect the UI to the existing REST and WebSocket backend.
```

### 5. Testing And Polish

```text
Read AGENTS.md and all docs.

Do a full review and polish pass for the chat app.

Check:
- backend build/tests
- frontend behavior
- rooms
- DMs
- uploads/downloads
- disconnected state
- empty states
- Indonesian copy consistency
- responsive layout
- visual match against docs/DESIGN_SYSTEM.md

Fix issues you find, keep changes focused, and summarize remaining limitations.
```

## Per-Task Checklist

For each implementation chat:
- Read the relevant docs.
- Inspect current code before editing.
- Make the smallest complete feature slice.
- Run relevant checks.
- Update docs when behavior changed.
- End with what changed, how to run it, what was tested, and what remains.

## Documentation Rule

If code and docs disagree, update one of them before finishing the task. Future agents should be able to trust the docs as current project context.
