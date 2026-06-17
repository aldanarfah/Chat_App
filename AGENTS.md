# Agent Context

## Project

This repo is being evolved from a simple Java WebSocket demo into a full MVP chat app.

The current app has:
- A Java HTTP server on port `8080`
- A Java WebSocket server on port `8887`
- A plain HTML/CSS/JS browser client
- No Maven/Gradle yet
- No database yet

The target app is a serious, responsive chat dashboard with:
- Username-based sessions
- Rooms
- Direct messages
- Realtime text messages
- Local file upload/download
- In-memory chat history for MVP
- Indonesian UI copy

## Required Reading

Before implementing changes, read the relevant docs:
- `docs/PRD.md` for product scope
- `docs/ARCHITECTURE.md` for backend/API/event direction
- `docs/DESIGN_SYSTEM.md` for visual/UI requirements
- `docs/FRONTEND_CONTEXT.md` for frontend behavior and structure
- `docs/WORKFLOW.md` for recommended task flow and prompts

## Engineering Rules

- Prefer small feature slices over giant rewrites.
- Preserve working behavior at each step.
- Keep the first MVP simple and maintainable.
- Use Maven for the future backend foundation.
- Keep storage in memory unless a later plan explicitly adds a database.
- Store uploaded files locally under `uploads/`.
- Keep frontend plain HTML/CSS/JS for MVP unless a later plan changes the stack.
- Update docs when behavior, endpoints, event names, or design rules change.

## UI Rules

- Build the app UI, not a landing page.
- Use Indonesian user-facing text.
- Follow the dark serious dashboard direction in `docs/DESIGN_SYSTEM.md`.
- The design is inspired by the reference screenshot, not an exact clone.
- Avoid the current centered demo-card layout once the UI implementation phase starts.
- Make desktop and mobile responsive states usable.
- Ensure text and controls do not overlap.

## Commands

Current legacy command:

```bash
./run.sh
```

Target Maven commands after the foundation phase:

```bash
mvn test
mvn package
mvn exec:java
```

If Maven commands are not available yet, the current task may need to create the Maven foundation first.

## Do Not Assume

- Do not assume real authentication exists.
- Do not assume message persistence across server restarts.
- Do not assume cloud storage.
- Do not assume the empty `.agents`, `.codex`, or `.git` directories are usable project context.

## Known Repo Note

The visible `.git` directory is empty in the current workspace, so Git may not recognize this folder as a repository. Treat version control setup or repair as a separate project setup task if needed.
