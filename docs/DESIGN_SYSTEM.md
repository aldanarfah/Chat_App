# Design System

## Direction

The UI should be inspired by the supplied dark chat dashboard reference. It should feel serious, polished, and app-like, not like the current simple demo card.

Use the reference for:
- Overall app shell structure
- Dark dashboard mood
- Left icon rail
- Conversation list/sidebar
- Active chat panel
- Rounded chat bubbles
- Compact icon buttons
- Clear active conversation state

Do not copy:
- Exact logos
- Exact brand names
- Exact user names
- Exact assets
- Exact screenshot composition

## Language

Use Indonesian for user-facing text.

Examples:
- `Chat`
- `Semua`
- `Grup`
- `Orang`
- `Ketik pesan...`
- `Sedang terhubung...`
- `Tidak ada percakapan`
- `Pilih percakapan`
- `File terlalu besar`
- `Format file tidak didukung`

## Layout

Desktop layout:
- Full viewport app shell
- Left icon rail: `72px`
- Conversation sidebar: `320px`
- Active chat panel: fills remaining width
- Header height: about `76px`
- Composer area: about `72px`
- Main timeline scrolls independently

Mobile layout:
- Show conversation list first
- After selecting a room or DM, show active chat view
- Active chat header includes a back button
- Composer stays fixed at the bottom of the active chat view
- Avoid horizontal scrolling

## Color Tokens

Recommended palette:

```css
:root {
  --bg: #11111b;
  --shell: #181824;
  --panel: #20202d;
  --panel-soft: #272737;
  --panel-raised: #303043;
  --border: #343448;
  --text: #f5f6ff;
  --text-muted: #a7a7b8;
  --text-soft: #d7d8e8;
  --primary: #635bff;
  --primary-hover: #766fff;
  --message-out: #58d5c9;
  --message-out-text: #071819;
  --success: #54d6c6;
  --danger: #ff8a9a;
  --warning: #ffcf70;
}
```

Keep the UI dark and balanced. Violet is the primary app accent. Teal is primarily for outgoing messages and online/presence accents.

## Typography

Use a modern sans stack:

```css
font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
```

Rules:
- No decorative serif font.
- No oversized landing-page hero type.
- Use compact app typography.
- Keep labels readable and short.
- Do not use negative letter spacing.

## Components

### Icon Rail

Contains:
- App mark
- Navigation buttons
- Settings button near bottom
- Current user avatar near bottom

States:
- Default icon button
- Hover
- Active with primary background

### Conversation Sidebar

Contains:
- Title: `Chat`
- Search action/icon
- New room/action icon
- Optional promo/status strip if useful
- Filter tabs: `Semua`, `Grup`, `Orang`
- Conversation list

Conversation item content:
- Avatar
- Name
- Last message preview
- Timestamp
- Unread badge when relevant
- Active state
- Typing state in teal

### Active Chat Header

Contains:
- Avatar
- Conversation title
- Status/member count
- Online indicator
- Action icons, such as file/info/call placeholders

For MVP, action icons may be visual placeholders if features are not implemented.

### Timeline

Contains:
- Date divider
- Incoming messages aligned left
- Outgoing messages aligned right
- Sender label for incoming group messages
- Timestamp metadata
- System messages centered and subtle

Outgoing bubble:
- Teal background
- Dark text
- Rounded corners with slightly distinct tail/corner shape

Incoming bubble:
- Raised dark surface
- Light text

### File Message Card

Content:
- File icon or thumbnail
- Original filename
- File size
- File type
- Download button

Image uploads may show a small preview if practical.

### Composer

Contains:
- Text input with placeholder `Ketik pesan...`
- Attachment icon button
- Send icon button

States:
- Disabled when disconnected
- Uploading state
- Error state when upload fails

## Interaction States

Required states:
- Connecting
- Connected
- Disconnected
- Empty conversation
- No messages yet
- Uploading file
- Upload failed
- Unsupported file
- Too-large file
- User typing

## Accessibility

- Icon buttons need accessible labels.
- Inputs need labels or equivalent aria labels.
- Text contrast must remain readable.
- Keyboard send with Enter should work.
- Do not rely on color alone for errors.

## Visual Acceptance Checklist

- The app looks like a full dashboard, not a centered demo.
- The dark shell resembles the reference direction.
- Sidebar and active chat have clear separation.
- Messages are easy to scan.
- File cards look intentional.
- Mobile layout is usable.
- No text overlaps controls.
- No horizontal scroll on mobile.
