# Product Requirements Document

## Product Goal

Build a full MVP chatting app from the current Java WebSocket demo. The app should feel like a real chat product: users can enter a name, join rooms, send direct messages, exchange realtime messages, and share files.

The project is also a learning-friendly Java networking app, so the implementation should stay understandable and not jump straight into production complexity.

## Target Users

- Students learning Java networking and WebSockets
- Small local/LAN demo groups
- Developers testing realtime chat concepts
- Future Codex/agent sessions extending the app feature by feature

## MVP Scope

The MVP includes:
- Username-based session creation
- Active user list
- Chat rooms
- Direct messages
- Realtime text messages over WebSocket
- Local file upload and download
- In-memory message history while the server is running
- Indonesian user-facing UI
- Responsive dark chat dashboard inspired by the supplied reference image

## Non-Goals For MVP

Do not include these in the first MVP:
- Real account registration/login
- Passwords
- Database persistence
- Message history after restart
- Cloud file storage
- End-to-end encryption
- Read receipts
- Message reactions
- Admin moderation
- Deployment setup
- Push notifications

## User Stories

- As a user, I can enter a display name and start chatting quickly.
- As a user, I can see available rooms.
- As a user, I can create or join a room.
- As a user, I can send messages in a room.
- As a user, I can send a direct message to another active user.
- As a user, I can upload a file and share it in a room or DM.
- As a user, I can download a shared file.
- As a user, I can see clear connection, empty, and error states.
- As a user on mobile, I can move between chat list and active conversation without layout breakage.

## Acceptance Criteria

- Two browser tabs can connect with different display names.
- Users in the same room receive each other's room messages.
- Users in different rooms do not receive unrelated room messages.
- A direct message appears only for the sender and target user.
- Uploaded allowed files appear as chat file messages.
- File download links work.
- Unsupported or too-large files are rejected with clear Indonesian UI feedback.
- Restarting the server clears message history, as expected for in-memory MVP storage.
- Desktop layout shows icon rail, conversation list, and active chat together.
- Mobile layout lets the user switch between conversation list and active chat.

## Product Tone

The app should feel serious, useful, and modern. It should not feel like a toy demo after the UI phase. The language should be friendly Indonesian, but the interface should remain compact and professional.
