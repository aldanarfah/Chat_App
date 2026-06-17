const CHAT_GENERAL_ROOM_ID = 'room-general';
const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;
const LAST_USERNAME_KEY = 'pemrogjar:last-username';

const icons = {
    attach: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M8.5 12.5 15 6a3.5 3.5 0 1 1 5 5l-8 8a5 5 0 1 1-7-7l7.5-7.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
    `,
    back: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M15 18 9 12l6-6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
    `,
    chat: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M7 18.5 3.5 21V6.5A2.5 2.5 0 0 1 6 4h12A2.5 2.5 0 0 1 20.5 6.5v8A2.5 2.5 0 0 1 18 17H7Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M8 9h8M8 13h5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
    `,
    compose: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M4 20h4l10-10a2.1 2.1 0 0 0-4-4L4 16v4Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M13 7l4 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
    `,
    file: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8l-5-5Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M14 3v5h5M9 13h6M9 17h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
    `,
    grid: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="4" y="4" width="6" height="6" rx="1.4" stroke="currentColor" stroke-width="1.8"/>
            <rect x="14" y="4" width="6" height="6" rx="1.4" stroke="currentColor" stroke-width="1.8"/>
            <rect x="4" y="14" width="6" height="6" rx="1.4" stroke="currentColor" stroke-width="1.8"/>
            <rect x="14" y="14" width="6" height="6" rx="1.4" stroke="currentColor" stroke-width="1.8"/>
        </svg>
    `,
    info: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle cx="12" cy="12" r="8.5" stroke="currentColor" stroke-width="1.8"/>
            <path d="M12 11.5v4M12 8.5h.01" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
    `,
    paperclip: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="m8 12 6-6a3 3 0 1 1 4 4l-8 8a5 5 0 0 1-7-7l8-8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
    `,
    profile: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle cx="12" cy="8" r="3.5" stroke="currentColor" stroke-width="1.8"/>
            <path d="M5 19a7 7 0 0 1 14 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
    `,
    plus: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
    `,
    search: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle cx="11" cy="11" r="6.5" stroke="currentColor" stroke-width="1.8"/>
            <path d="m16 16 4.5 4.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
    `,
    send: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M4 11.5 20 4l-4.5 16-3.5-6L4 11.5Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M11.7 14 20 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
    `,
    settings: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="m12 2.8 2 1.1 2.3-.3 1.3 1.9 2.2.8.1 2.4 1.4 1.8-1 2.1 1 2.1-1.4 1.8-.1 2.4-2.2.8-1.3 1.9-2.3-.3-2 1.1-2-1.1-2.3.3-1.3-1.9-2.2-.8-.1-2.4L2.6 14l1-2.1-1-2.1L4 8l.1-2.4 2.2-.8 1.3-1.9 2.3.3 2-1.1Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <circle cx="12" cy="12" r="3.2" stroke="currentColor" stroke-width="1.8"/>
        </svg>
    `,
    users: `
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 12a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7ZM16.5 10.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3.5 19a5.5 5.5 0 0 1 11 0M14 19a4.5 4.5 0 0 1 6.5-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
    `
};

const elements = {
    appShell: document.getElementById('appShell'),
    attachButton: document.getElementById('attachButton'),
    chatHeader: document.getElementById('chatHeader'),
    conversationCountChip: document.getElementById('conversationCountChip'),
    composerForm: document.getElementById('composerForm'),
    connectButton: document.getElementById('connectButton'),
    connectionStrip: document.getElementById('connectionStrip'),
    conversationList: document.getElementById('conversationList'),
    conversationSearch: document.getElementById('conversationSearch'),
    conversationSectionLabel: document.getElementById('conversationSectionLabel'),
    createRoomButton: document.getElementById('createRoomButton'),
    createRoomRailButton: document.getElementById('createRoomRailButton'),
    fileInput: document.getElementById('fileInput'),
    filterTabs: document.getElementById('filterTabs'),
    focusSearchButton: document.getElementById('focusSearchButton'),
    messageInput: document.getElementById('messageInput'),
    messageTimeline: document.getElementById('messageTimeline'),
    nameInput: document.getElementById('nameInput'),
    railUserChip: document.getElementById('railUserChip'),
    sendButton: document.getElementById('sendButton'),
    sessionAvatarPreview: document.getElementById('sessionAvatarPreview'),
    sessionForm: document.getElementById('sessionForm'),
    sessionHint: document.getElementById('sessionHint'),
    sessionPreviewName: document.getElementById('sessionPreviewName'),
    sessionView: document.getElementById('sessionView'),
    toastRegion: document.getElementById('toastRegion'),
    typingState: document.getElementById('typingState'),
    uploadState: document.getElementById('uploadState')
};

const state = {
    activeConversationId: null,
    activeConversationType: null,
    connected: false,
    connectionMode: 'idle',
    draftByConversation: {},
    filter: 'all',
    joinedRooms: {},
    messagesByConversation: {},
    mobileView: window.innerWidth <= 960 ? 'list' : 'chat',
    reconnectAttempts: 0,
    reconnectTimerId: null,
    roomMembers: {},
    rooms: [],
    searchQuery: '',
    session: null,
    shouldReconnect: false,
    socket: null,
    toasts: [],
    upload: { status: 'idle', fileName: '' },
    userDirectory: {},
    users: [],
    unreadByConversation: {}
};

const timeFormatter = new Intl.DateTimeFormat('id-ID', {
    hour: '2-digit',
    minute: '2-digit'
});

const dayFormatter = new Intl.DateTimeFormat('id-ID', {
    day: 'numeric',
    month: 'long',
    weekday: 'long'
});

applyIcons();
hydrateLastUsername();
render();

elements.sessionForm.addEventListener('submit', handleSessionSubmit);
elements.nameInput.addEventListener('input', renderSessionPreview);
elements.focusSearchButton.addEventListener('click', () => elements.conversationSearch.focus());
elements.createRoomButton.addEventListener('click', handleCreateRoom);
elements.createRoomRailButton.addEventListener('click', handleCreateRoom);
elements.conversationSearch.addEventListener('input', (event) => {
    state.searchQuery = event.target.value.trim().toLowerCase();
    renderConversationList();
});
elements.filterTabs.addEventListener('click', (event) => {
    const target = event.target.closest('[data-filter]');
    if (!target) {
        return;
    }

    state.filter = target.dataset.filter;
    renderFilterTabs();
    renderConversationList();
});
elements.composerForm.addEventListener('submit', handleSendMessage);
elements.attachButton.addEventListener('click', () => {
    if (!canCompose()) {
        pushToast(connectionFeedbackMessage(), 'error');
        return;
    }
    elements.fileInput.click();
});
elements.fileInput.addEventListener('change', handleFileSelection);
elements.messageInput.addEventListener('input', handleComposerInput);
elements.messageInput.addEventListener('keydown', (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        handleSendMessage(event);
    }
});
window.addEventListener('resize', handleViewportResize);
window.addEventListener('beforeunload', () => {
    state.shouldReconnect = false;
    clearReconnectTimer();
    if (state.socket) {
        state.socket.close();
    }
});

function applyIcons() {
    const iconMap = {
        chat: icons.chat,
        compose: icons.compose,
        file: icons.file,
        grid: icons.grid,
        info: icons.info,
        paperclip: icons.paperclip,
        profile: icons.profile,
        plus: icons.plus,
        search: icons.search,
        send: icons.send,
        settings: icons.settings,
        users: icons.users
    };

    document.querySelectorAll('[data-icon]').forEach((node) => {
        const iconMarkup = iconMap[node.dataset.icon];
        if (iconMarkup) {
            node.innerHTML = iconMarkup;
        }
    });
}

function hydrateLastUsername() {
    try {
        const lastUsername = window.localStorage.getItem(LAST_USERNAME_KEY);
        if (lastUsername) {
            elements.nameInput.value = lastUsername;
        }
    } catch (error) {
        console.warn('Tidak dapat membaca localStorage:', error);
    }

    renderSessionPreview();
}

function renderSessionPreview() {
    const username = elements.nameInput.value.trim();
    const displayName = username || 'Nama Anda';

    if (elements.sessionPreviewName) {
        elements.sessionPreviewName.textContent = displayName;
    }

    if (elements.sessionAvatarPreview) {
        elements.sessionAvatarPreview.textContent = initials(displayName || 'NA');
    }
}

async function handleSessionSubmit(event) {
    event.preventDefault();

    const username = elements.nameInput.value.trim();
    if (!username) {
        updateSessionHint('Nama tampilan wajib diisi.', 'error');
        elements.nameInput.focus();
        return;
    }

    elements.connectButton.disabled = true;
    updateSessionHint('Membuat sesi dan menyiapkan koneksi...', 'info');

    try {
        const session = await createOrTakeOverSession(username);
        applyAuthenticatedSession(session, username, 'Sesi berhasil disiapkan. Menghubungkan ke server realtime...');
    } catch (error) {
        updateSessionHint(error.message, 'error');
    } finally {
        elements.connectButton.disabled = false;
    }
}

async function createOrTakeOverSession(username) {
    try {
        return await postJson('/api/session', { username });
    } catch (error) {
        if (error.code !== 'username_in_use' || !error.data?.canTakeOver) {
            throw error;
        }

        const confirmed = window.confirm(
            `Nama "${username}" masih aktif di perangkat lain.\n\nAmbil alih sesi itu dan masuk dengan nama yang sama?`
        );
        if (!confirmed) {
            throw new Error('Masuk dibatalkan. Gunakan nama lain atau ambil alih sesi lama untuk lanjut.');
        }

        updateSessionHint('Mengambil alih sesi lama dan menyiapkan koneksi baru...', 'info');
        return postJson('/api/session', { username, forceTakeover: true });
    }
}

function applyAuthenticatedSession(session, username, successMessage) {
    state.session = session;
    state.shouldReconnect = true;
    updateSessionHint(successMessage, 'success');
    rememberUsername(username);
    renderSessionState();
    connectSocket();
}

function rememberUsername(username) {
    try {
        window.localStorage.setItem(LAST_USERNAME_KEY, username);
    } catch (error) {
        console.warn('Tidak dapat menyimpan nama terakhir:', error);
    }
}

function updateSessionHint(message, tone) {
    elements.sessionHint.textContent = message;
    elements.sessionHint.dataset.tone = tone;
}

function connectSocket() {
    if (!state.session) {
        return;
    }
    if (state.socket && (state.socket.readyState === WebSocket.OPEN || state.socket.readyState === WebSocket.CONNECTING)) {
        return;
    }

    clearReconnectTimer();
    state.connected = false;
    state.joinedRooms = {};
    setConnectionMode(state.reconnectAttempts > 0 ? 'reconnecting' : 'connecting');

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const socketUrl = `${protocol}//${window.location.hostname}:8887/ws?sessionId=${encodeURIComponent(state.session.sessionId)}`;
    const socket = new WebSocket(socketUrl);
    state.socket = socket;

    socket.addEventListener('open', () => {
        state.connected = true;
        state.reconnectAttempts = 0;
        setConnectionMode('connected');
        renderComposerState();
    });

    socket.addEventListener('message', (event) => {
        try {
            const message = JSON.parse(event.data);
            handleSocketEvent(message);
        } catch (error) {
            pushToast('Pesan realtime tidak dapat dibaca.', 'error');
        }
    });

    socket.addEventListener('close', () => {
        state.connected = false;
        state.socket = null;
        state.joinedRooms = {};
        renderComposerState();

        if (state.shouldReconnect && state.session) {
            scheduleReconnect();
            return;
        }

        setConnectionMode('disconnected');
    });

    socket.addEventListener('error', () => {
        state.connected = false;
        setConnectionMode('error');
    });
}

function scheduleReconnect() {
    clearReconnectTimer();
    state.reconnectAttempts += 1;
    setConnectionMode('reconnecting');
    const delay = Math.min(1200 + (state.reconnectAttempts - 1) * 800, 5000);
    state.reconnectTimerId = window.setTimeout(connectSocket, delay);
}

function clearReconnectTimer() {
    if (state.reconnectTimerId !== null) {
        window.clearTimeout(state.reconnectTimerId);
        state.reconnectTimerId = null;
    }
}

function handleSocketEvent(message) {
    const type = message?.type;
    const payload = message?.payload ?? {};

    switch (type) {
        case 'session.ready':
            handleSessionReady(payload);
            break;
        case 'presence.joined':
        case 'presence.left':
            syncUsers(payload.activeUsers ?? []);
            renderConversationList();
            renderChatHeader();
            if (payload.user?.sessionId && payload.user.sessionId !== state.session?.sessionId) {
                pushToast(type === 'presence.joined'
                    ? `${payload.user?.username ?? 'Pengguna'} sedang online.`
                    : `${payload.user?.username ?? 'Pengguna'} keluar dari chat.`, 'info');
            }
            break;
        case 'room.joined':
            handleRoomJoined(payload);
            break;
        case 'room.created':
            if (payload.room) {
                mergeRoom(payload.room);
                renderConversationList();
                renderChatHeader();
            }
            break;
        case 'room.left':
            if (payload.roomId) {
                delete state.joinedRooms[payload.roomId];
                state.roomMembers[payload.roomId] = payload.members ?? [];
                renderConversationList();
                renderChatHeader();
            }
            break;
        case 'room.membership.updated':
            handleRoomMembershipUpdated(payload);
            break;
        case 'room.message.receive':
        case 'dm.message.receive':
            handleIncomingMessage(payload);
            break;
        case 'error':
            pushToast(payload.message || 'Terjadi kesalahan realtime.', 'error');
            if (payload.code === 'session_taken_over') {
                state.shouldReconnect = false;
                state.session = null;
                state.connected = false;
                state.socket = null;
                updateSessionHint('Sesi ini diambil alih dari perangkat lain. Masuk lagi untuk melanjutkan.', 'error');
                render();
            } else if (payload.code === 'invalid_session') {
                state.shouldReconnect = false;
                setConnectionMode('error');
            }
            break;
        default:
            break;
    }
}

async function handleSessionReady(payload) {
    syncUsers(payload.activeUsers ?? []);
    syncRooms(payload.rooms ?? []);
    syncDirectConversations(payload.directConversations ?? []);
    renderSessionState();
    await refreshCollections();
    ensureInitialConversation();
}

function syncDirectConversations(directConversations) {
    for (const conversation of directConversations) {
        if (!conversation?.conversationId) {
            continue;
        }

        if (conversation.targetSessionId && conversation.targetName) {
            const existingUser = state.userDirectory[conversation.targetSessionId] || {};
            state.userDirectory[conversation.targetSessionId] = {
                ...existingUser,
                sessionId: conversation.targetSessionId,
                username: conversation.targetName,
                active: Boolean(conversation.active)
            };
        }

        mergeMessages(conversation.conversationId, conversation.messages ?? []);
    }
}

function handleRoomJoined(payload) {
    const room = payload.room;
    if (!room) {
        return;
    }

    state.joinedRooms[room.roomId] = true;
    mergeRoom(room);
    state.roomMembers[room.roomId] = payload.members ?? [];
    mergeMessages(room.roomId, payload.recentMessages ?? []);
    renderConversationList();
    renderChatHeader();
    renderTimeline();
    if (room.roomId === state.activeConversationId) {
        scrollTimelineToBottom();
    }
}

function handleRoomMembershipUpdated(payload) {
    if (!payload.room) {
        return;
    }

    mergeRoom(payload.room);
    state.roomMembers[payload.room.roomId] = payload.members ?? [];
    renderConversationList();
    renderChatHeader();
}

function handleIncomingMessage(message) {
    if (!message?.conversationId) {
        return;
    }

    mergeMessages(message.conversationId, [message]);

    if (message.conversationType === 'direct') {
        const otherSessionId = resolveDirectTargetSessionId(message);
        if (otherSessionId) {
            const knownUser = state.userDirectory[otherSessionId] || {
                sessionId: otherSessionId,
                username: isCurrentUserMessage(message) ? 'Pengguna' : message.senderName,
                active: true
            };
            state.userDirectory[otherSessionId] = knownUser;
        }
    }

    if (message.conversationId !== state.activeConversationId || document.hidden) {
        state.unreadByConversation[message.conversationId] = (state.unreadByConversation[message.conversationId] || 0) + 1;
    } else {
        state.unreadByConversation[message.conversationId] = 0;
    }

    renderConversationList();
    if (message.conversationId === state.activeConversationId) {
        renderTimeline();
        scrollTimelineToBottom();
    }
}

async function refreshCollections() {
    try {
        const [roomsResponse, usersResponse] = await Promise.all([
            fetchJson('/api/rooms'),
            fetchJson('/api/users')
        ]);
        syncRooms(roomsResponse.rooms ?? []);
        syncUsers(usersResponse.users ?? []);
    } catch (error) {
        pushToast(error.message, 'error');
    }
}

function syncRooms(rooms) {
    state.rooms = [...rooms].sort((left, right) => left.name.localeCompare(right.name, 'id'));
}

function syncUsers(users) {
    const activeSessionIds = new Set(users.map((user) => user.sessionId));
    for (const [sessionId, knownUser] of Object.entries(state.userDirectory)) {
        if (sessionId === state.session?.sessionId || activeSessionIds.has(sessionId)) {
            continue;
        }

        state.userDirectory[sessionId] = {
            ...knownUser,
            active: false
        };
    }

    state.users = [...users]
        .map((user) => ({ ...user, active: Boolean(user.active) }))
        .sort((left, right) => left.username.localeCompare(right.username, 'id'));

    for (const user of state.users) {
        state.userDirectory[user.sessionId] = user;
    }
}

function mergeRoom(room) {
    const existing = state.rooms.find((item) => item.roomId === room.roomId);
    if (existing) {
        Object.assign(existing, room);
        return;
    }
    state.rooms = [...state.rooms, room].sort((left, right) => left.name.localeCompare(right.name, 'id'));
}

function mergeMessages(conversationId, incomingMessages) {
    const existingMessages = state.messagesByConversation[conversationId] ?? [];
    const messagesById = new Map(existingMessages.map((message) => [message.id, message]));

    for (const message of incomingMessages) {
        if (message?.id) {
            messagesById.set(message.id, message);
        }
    }

    state.messagesByConversation[conversationId] = [...messagesById.values()].sort((left, right) => {
        return parseTimestamp(left.createdAt) - parseTimestamp(right.createdAt);
    });
}

function ensureInitialConversation() {
    const availableConversations = buildConversations();
    if (!availableConversations.length) {
        render();
        return;
    }

    const activeConversationStillExists = availableConversations.some((conversation) => {
        return conversation.id === state.activeConversationId && conversation.type === state.activeConversationType;
    });

    if (activeConversationStillExists) {
        if (state.activeConversationType === 'room') {
            ensureRoomJoined(state.activeConversationId);
        }
        render();
        return;
    }

    const generalRoom = availableConversations.find((conversation) => conversation.id === CHAT_GENERAL_ROOM_ID && conversation.type === 'room');
    const firstConversation = generalRoom || availableConversations[0];
    selectConversation(firstConversation.id, firstConversation.type);
}

function buildConversations() {
    const roomConversations = state.rooms.map((room) => buildRoomConversation(room));
    const directConversations = buildDirectConversations();
    return [...roomConversations, ...directConversations].sort(compareConversations);
}

function buildRoomConversation(room) {
    const lastMessage = lastMessageForConversation(room.roomId);
    return {
        id: room.roomId,
        type: 'room',
        name: room.name,
        memberCount: room.memberCount ?? 0,
        lastMessage,
        preview: conversationPreview(room.roomId, 'room', lastMessage),
        timestamp: lastMessage?.createdAt ?? room.createdAt,
        unread: state.unreadByConversation[room.roomId] || 0
    };
}

function buildDirectConversations() {
    if (!state.session) {
        return [];
    }

    const directConversationIds = new Set();

    for (const user of state.users) {
        if (user.sessionId === state.session?.sessionId) {
            continue;
        }
        directConversationIds.add(directConversationId(state.session.sessionId, user.sessionId));
    }

    for (const conversationId of Object.keys(state.messagesByConversation)) {
        if (conversationId.startsWith('dm:')) {
            directConversationIds.add(conversationId);
        }
    }

    return [...directConversationIds].map((conversationId) => {
        const targetSessionId = otherDirectParticipant(conversationId);
        const user = targetSessionId ? state.userDirectory[targetSessionId] : null;
        const lastMessage = lastMessageForConversation(conversationId);
        return {
            id: conversationId,
            type: 'dm',
            targetSessionId,
            name: user?.username ?? 'Pengguna',
            online: Boolean(user?.active),
            lastMessage,
            preview: conversationPreview(conversationId, 'dm', lastMessage),
            timestamp: lastMessage?.createdAt ?? user?.connectedAt ?? user?.createdAt ?? '',
            unread: state.unreadByConversation[conversationId] || 0
        };
    });
}

function compareConversations(left, right) {
    const leftTimestamp = parseTimestamp(left.timestamp || '');
    const rightTimestamp = parseTimestamp(right.timestamp || '');
    if (leftTimestamp !== rightTimestamp) {
        return rightTimestamp - leftTimestamp;
    }
    if (left.type !== right.type) {
        return left.type === 'dm' ? 1 : -1;
    }
    return left.name.localeCompare(right.name, 'id');
}

function conversationPreview(conversationId, type, lastMessage) {
    if (state.activeConversationId === conversationId) {
        const draft = (state.draftByConversation[conversationId] || '').trim();
        if (draft) {
            return 'Anda sedang mengetik...';
        }
    }

    if (!lastMessage) {
        return type === 'room' ? 'Belum ada pesan di room ini.' : 'Mulai percakapan langsung.';
    }

    if (lastMessage.file && lastMessage.text) {
        return `${messagePrefix(lastMessage)} mengirim file dan pesan`;
    }
    if (lastMessage.file) {
        return `${messagePrefix(lastMessage)} mengirim file`;
    }
    return `${messagePrefix(lastMessage)} ${lastMessage.text}`;
}

function messagePrefix(message) {
    if (isCurrentUserMessage(message)) {
        return 'Anda';
    }
    return message.senderName;
}

function lastMessageForConversation(conversationId) {
    const messages = state.messagesByConversation[conversationId] ?? [];
    return messages[messages.length - 1] || null;
}

function render() {
    renderSessionState();
    renderRailUserChip();
    renderConnectionStrip();
    renderFilterTabs();
    renderConversationList();
    renderChatHeader();
    renderTimeline();
    renderComposerState();
    renderToasts();
}

function renderSessionState() {
    const hasSession = Boolean(state.session);
    elements.sessionView.hidden = hasSession;
    if (hasSession) {
        elements.appShell.dataset.mobileView = state.mobileView;
    }
}

function renderRailUserChip() {
    elements.railUserChip.textContent = initials(state.session?.username || '--');
    elements.railUserChip.setAttribute('aria-label', state.session ? `Pengguna aktif ${state.session.username}` : 'Belum ada sesi');
}

function renderConnectionStrip() {
    const content = {
        idle: 'Belum terhubung. Buat sesi untuk mulai chat.',
        connecting: `Sedang menyambungkan ${state.session?.username || 'pengguna'} ke server realtime...`,
        connected: `${state.session?.username || 'Pengguna'} sudah aktif. Room dan pesan langsung siap dipakai.`,
        reconnecting: 'Koneksi terputus. Mencoba terhubung ulang ke server realtime...',
        disconnected: 'Koneksi terputus. Tunggu sebentar atau muat ulang halaman.',
        error: 'Server realtime belum merespons. Pastikan WebSocket aktif di port 8887.'
    };

    elements.connectionStrip.className = `connection-strip connection-strip--${state.connectionMode}`;
    elements.connectionStrip.textContent = content[state.connectionMode];
}

function renderFilterTabs() {
    elements.filterTabs.querySelectorAll('[data-filter]').forEach((button) => {
        const isActive = button.dataset.filter === state.filter;
        button.classList.toggle('filter-tab--active', isActive);
        button.setAttribute('aria-selected', String(isActive));
    });
}

function renderConversationList() {
    const conversations = filteredConversations();
    renderConversationListMeta(conversations.length);
    if (!conversations.length) {
        elements.conversationList.replaceChildren(createEmptyListState());
        return;
    }

    const items = conversations.map((conversation) => createConversationItem(conversation));
    elements.conversationList.replaceChildren(...items);
}

function renderConversationListMeta(count) {
    const labelByFilter = {
        all: 'Semua Percakapan',
        room: 'Room Aktif',
        dm: 'Pesan Langsung'
    };
    elements.conversationSectionLabel.textContent = labelByFilter[state.filter] || 'Percakapan';
    elements.conversationCountChip.textContent = String(count);
}

function filteredConversations() {
    return buildConversations().filter((conversation) => {
        if (state.filter === 'room' && conversation.type !== 'room') {
            return false;
        }
        if (state.filter === 'dm' && conversation.type !== 'dm') {
            return false;
        }

        if (!state.searchQuery) {
            return true;
        }

        return `${conversation.name} ${conversation.preview}`.toLowerCase().includes(state.searchQuery);
    });
}

function createConversationItem(conversation) {
    const item = document.createElement('button');
    item.type = 'button';
    item.className = 'conversation-item';

    if (conversation.id === state.activeConversationId && conversation.type === state.activeConversationType) {
        item.classList.add('conversation-item--active');
    }

    item.addEventListener('click', () => selectConversation(conversation.id, conversation.type));

    const avatar = document.createElement('div');
    avatar.className = `avatar ${conversation.type === 'room' ? 'avatar--room' : ''}`;
    avatar.textContent = conversation.type === 'room' ? '#' : initials(conversation.name);

    const body = document.createElement('div');
    body.className = 'conversation-item__body';

    const topRow = document.createElement('div');
    topRow.className = 'conversation-item__row';

    const title = document.createElement('div');
    title.className = 'conversation-item__title';
    title.textContent = conversation.name;

    const meta = document.createElement('div');
    meta.className = 'conversation-item__meta';
    meta.textContent = formatConversationMeta(conversation);

    topRow.append(title, meta);

    const preview = document.createElement('div');
    preview.className = 'conversation-item__preview';
    preview.textContent = conversation.preview;
    if (conversation.preview === 'Anda sedang mengetik...') {
        preview.classList.add('conversation-item__preview--typing');
    }

    body.append(topRow, preview);

    const trailing = document.createElement('div');
    trailing.className = 'conversation-item__trailing';

    if (conversation.type === 'dm') {
        const presence = document.createElement('span');
        presence.className = 'presence-pill';
        const dot = document.createElement('span');
        dot.className = `presence-dot ${conversation.online ? 'presence-dot--online' : ''}`;
        const label = document.createElement('span');
        label.textContent = conversation.online ? 'Aktif' : 'Tidak aktif';
        presence.append(dot, label);
        trailing.appendChild(presence);
    } else {
        const badge = document.createElement('span');
        badge.className = 'tag-badge presence-pill';
        badge.textContent = `${conversation.memberCount} anggota`;
        trailing.appendChild(badge);
    }

    if (conversation.unread > 0) {
        const unread = document.createElement('span');
        unread.className = 'unread-badge';
        unread.textContent = conversation.unread > 9 ? '9+' : String(conversation.unread);
        trailing.appendChild(unread);
    }

    item.append(avatar, body, trailing);
    return item;
}

function formatConversationMeta(conversation) {
    if (conversation.timestamp) {
        return formatTime(conversation.timestamp);
    }
    return conversation.type === 'room' ? 'Room' : 'Langsung';
}

function renderChatHeader() {
    const conversation = activeConversation();
    if (!conversation) {
        const [emptyMain, emptyActions] = createEmptyHeader();
        elements.chatHeader.replaceChildren(emptyMain, emptyActions);
        return;
    }

    const main = document.createElement('div');
    main.className = 'chat-header__main';

    const backButton = document.createElement('button');
    backButton.type = 'button';
    backButton.className = 'ghost-icon-button back-button';
    backButton.setAttribute('aria-label', 'Kembali ke daftar chat');
    backButton.innerHTML = icons.back;
    backButton.addEventListener('click', () => {
        state.mobileView = 'list';
        renderSessionState();
        renderComposerState();
    });

    const avatar = document.createElement('div');
    avatar.className = `header-avatar ${conversation.type === 'room' ? 'header-avatar--room' : ''}`;
    avatar.textContent = conversation.type === 'room' ? '#' : initials(conversation.name);

    const text = document.createElement('div');
    text.className = 'chat-header__text';

    const titleRow = document.createElement('div');
    titleRow.className = 'chat-title';
    const title = document.createElement('h3');
    title.textContent = conversation.name;
    titleRow.appendChild(title);

    if (conversation.type === 'room') {
        const badge = document.createElement('span');
        badge.className = 'chat-badge';
        badge.textContent = state.joinedRooms[conversation.id] ? 'Room Aktif' : 'Room';
        titleRow.appendChild(badge);
    } else {
        titleRow.appendChild(createPresencePill(conversation.online ? 'Sedang aktif' : 'Tidak aktif', conversation.online));
    }

    const subtitle = document.createElement('div');
    subtitle.className = 'chat-subtitle';
    const subtitleText = activeSubtitle(conversation);
    subtitle.textContent = subtitleText;
    if (subtitleText === 'Anda sedang mengetik...') {
        subtitle.classList.add('chat-subtitle--typing');
    }

    text.append(titleRow);
    if (conversation.type === 'room') {
        text.appendChild(createRoomStatline(conversation));
    }
    text.appendChild(subtitle);
    main.append(backButton, avatar, text);

    const actions = document.createElement('div');
    actions.className = 'chat-header__actions';

    const actionIcons = [icons.paperclip, icons.users, icons.info];
    const actionLabels = ['Lampiran', 'Peserta', 'Info'];
    actionIcons.forEach((iconMarkup, index) => {
        const action = document.createElement('button');
        action.type = 'button';
        action.className = 'ghost-icon-button';
        action.setAttribute('aria-label', actionLabels[index]);
        action.innerHTML = iconMarkup;
        action.disabled = true;
        actions.appendChild(action);
    });

    elements.chatHeader.replaceChildren(main, actions);
}

function activeSubtitle(conversation) {
    const draft = (state.draftByConversation[conversation.id] || '').trim();
    if (draft && state.activeConversationId === conversation.id) {
        return 'Anda sedang mengetik...';
    }

    if (conversation.type === 'room') {
        const members = state.roomMembers[conversation.id]?.length ?? conversation.memberCount ?? 0;
        return state.joinedRooms[conversation.id]
            ? `${members} anggota • Anda ikut di room ini`
            : `${members} anggota • Pilih room ini untuk bergabung otomatis`;
    }

    return conversation.online ? 'Pengguna sedang online' : 'Pengguna sedang tidak aktif';
}

function createRoomStatline(conversation) {
    const statline = document.createElement('div');
    statline.className = 'chat-statline';

    const members = state.roomMembers[conversation.id] ?? [];
    const memberCount = members.length || conversation.memberCount || 0;
    const onlineCount = members.filter((member) => member?.active).length;

    if (members.length) {
        statline.appendChild(createAvatarStack(members.slice(0, 4)));
    }

    const membersText = document.createElement('span');
    membersText.className = 'chat-stat';
    membersText.textContent = `${memberCount} anggota`;
    statline.appendChild(membersText);

    const divider = document.createElement('span');
    divider.className = 'chat-stat';
    divider.textContent = '•';
    statline.appendChild(divider);

    const onlineText = document.createElement('span');
    onlineText.className = 'chat-stat';
    onlineText.innerHTML = `<strong>${onlineCount}</strong> online`;
    statline.appendChild(onlineText);

    return statline;
}

function createAvatarStack(members) {
    const stack = document.createElement('div');
    stack.className = 'chat-avatar-stack';

    members.forEach((member) => {
        const avatar = document.createElement('span');
        avatar.className = 'chat-avatar-stack__item';
        avatar.textContent = initials(member.username || 'U');
        stack.appendChild(avatar);
    });

    return stack;
}

function createPresencePill(label, online) {
    const pill = document.createElement('span');
    pill.className = 'presence-pill';
    const dot = document.createElement('span');
    dot.className = `presence-dot ${online ? 'presence-dot--online' : ''}`;
    const text = document.createElement('span');
    text.textContent = label;
    pill.append(dot, text);
    return pill;
}

function renderTimeline() {
    const conversation = activeConversation();
    if (!conversation) {
        elements.messageTimeline.replaceChildren(createEmptyChatState(
            'Pilih percakapan',
            'Pilih room atau orang dari daftar di samping untuk melihat timeline pesan.'
        ));
        return;
    }

    const messages = state.messagesByConversation[conversation.id] ?? [];
    if (!messages.length) {
        elements.messageTimeline.replaceChildren(createEmptyChatState(
            'Belum ada pesan',
            conversation.type === 'room'
                ? 'Mulai obrolan room ini atau kirim file pertama untuk membuka percakapan.'
                : 'Mulai direct message pertama Anda di sini.'
        ));
        return;
    }

    const nodes = [];
    let lastDateLabel = '';

    for (const message of messages) {
        const dateLabel = formatDay(message.createdAt);
        if (dateLabel !== lastDateLabel) {
            lastDateLabel = dateLabel;
            const divider = document.createElement('div');
            divider.className = 'timeline-divider';
            divider.textContent = dateLabel;
            nodes.push(divider);
        }

        nodes.push(createMessageRow(message, conversation.type));
    }

    elements.messageTimeline.replaceChildren(...nodes);
}

function createMessageRow(message, conversationType) {
    const row = document.createElement('div');
    const outgoing = isCurrentUserMessage(message);
    row.className = `message-row ${outgoing ? 'message-row--outgoing' : ''}`;

    const avatar = document.createElement('div');
    avatar.className = 'message-avatar';
    avatar.textContent = outgoing ? initials(state.session?.username || 'A') : initials(message.senderName || 'U');

    const stack = document.createElement('div');
    stack.className = 'message-stack';

    const bubble = document.createElement('div');
    bubble.className = 'message-bubble';

    if (!outgoing && conversationType === 'room') {
        const sender = document.createElement('div');
        sender.className = 'message-sender';
        sender.textContent = message.senderName;
        bubble.appendChild(sender);
    }

    if (message.text) {
        const text = document.createElement('div');
        text.className = 'message-text';
        text.textContent = message.text;
        bubble.appendChild(text);
    }

    if (message.file) {
        bubble.appendChild(createFileCard(message.file));
    }

    const meta = document.createElement('div');
    meta.className = 'message-meta';
    meta.textContent = formatTime(message.createdAt);
    bubble.appendChild(meta);

    stack.appendChild(bubble);
    row.append(avatar, stack);
    return row;
}

function createFileCard(file) {
    const card = document.createElement('div');
    card.className = 'message-file';

    const top = document.createElement('div');
    top.className = 'message-file__top';

    const icon = document.createElement('div');
    icon.className = 'file-icon';
    icon.innerHTML = icons.file;

    const details = document.createElement('div');

    const name = document.createElement('div');
    name.className = 'message-file__name';
    name.textContent = file.filename;

    const meta = document.createElement('div');
    meta.className = 'file-meta';
    meta.textContent = `${friendlyFileType(file.contentType)} • ${formatFileSize(file.sizeBytes)}`;

    details.append(name, meta);
    top.append(icon, details);
    card.appendChild(top);

    if (isImageFile(file.contentType)) {
        const preview = document.createElement('div');
        preview.className = 'file-preview';
        const image = document.createElement('img');
        image.src = file.downloadUrl;
        image.alt = file.filename;
        preview.appendChild(image);
        card.appendChild(preview);
    }

    const download = document.createElement('a');
    download.className = 'download-link';
    download.href = file.downloadUrl;
    download.textContent = 'Unduh file';
    download.setAttribute('download', file.filename);
    card.appendChild(download);

    return card;
}

function renderComposerState() {
    const conversation = activeConversation();
    const canType = canCompose();
    const currentDraft = conversation ? state.draftByConversation[conversation.id] || '' : '';

    elements.messageInput.disabled = !canType;
    elements.messageInput.placeholder = composerPlaceholder();
    elements.messageInput.value = currentDraft;
    autoResizeComposer();

    elements.sendButton.disabled = !canType;
    elements.attachButton.disabled = !canType;

    elements.typingState.textContent = typingStatusText(conversation);
    elements.uploadState.textContent = uploadStatusText();
}

function composerPlaceholder() {
    if (!state.session) {
        return 'Masuk dulu untuk mulai chat...';
    }
    if (!state.connected) {
        return 'Koneksi terputus...';
    }
    if (!state.activeConversationId) {
        return 'Pilih percakapan...';
    }
    return 'Ketik pesan...';
}

function typingStatusText(conversation) {
    if (state.upload.status === 'uploading') {
        return 'Mengunggah file...';
    }
    if (!conversation) {
        return 'Pilih percakapan untuk mulai mengirim.';
    }

    const draft = (state.draftByConversation[conversation.id] || '').trim();
    if (draft) {
        return 'Anda sedang mengetik...';
    }

    if (conversation.type === 'room') {
        return state.joinedRooms[conversation.id]
            ? 'Tekan Enter untuk kirim. Shift+Enter untuk baris baru.'
            : 'Room akan digabung otomatis saat dipilih.';
    }
    return conversation.online
        ? 'Pengguna sedang aktif dan siap menerima pesan.'
        : 'Pengguna sedang tidak aktif. Pesan baru bisa dikirim saat mereka online.';
}

function uploadStatusText() {
    if (state.upload.status === 'uploading') {
        return `${state.upload.fileName} sedang diunggah`;
    }
    if (state.upload.status === 'error') {
        return 'Unggah gagal';
    }
    if (state.upload.status === 'done') {
        return 'File berhasil dikirim';
    }
    return '';
}

function renderToasts() {
    const nodes = state.toasts.map((toast) => {
        const item = document.createElement('div');
        item.className = `toast toast--${toast.tone}`;
        item.textContent = toast.message;
        return item;
    });
    elements.toastRegion.replaceChildren(...nodes);
}

function pushToast(message, tone = 'info') {
    const toastId = window.crypto?.randomUUID
        ? window.crypto.randomUUID()
        : `toast_${Date.now()}_${Math.random().toString(16).slice(2)}`;
    const toast = { id: toastId, message, tone };
    state.toasts = [...state.toasts, toast].slice(-4);
    renderToasts();
    window.setTimeout(() => {
        state.toasts = state.toasts.filter((item) => item.id !== toast.id);
        renderToasts();
    }, 3600);
}

function selectConversation(conversationId, conversationType) {
    state.activeConversationId = conversationId;
    state.activeConversationType = conversationType;
    state.unreadByConversation[conversationId] = 0;
    state.mobileView = 'chat';

    if (conversationType === 'room') {
        ensureRoomJoined(conversationId);
    }

    render();
    scrollTimelineToBottom();
    elements.messageInput.focus();
}

function ensureRoomJoined(roomId) {
    if (!state.connected || state.joinedRooms[roomId]) {
        return;
    }

    sendSocketEvent('room.join', { roomId });
}

function handleComposerInput(event) {
    const conversation = activeConversation();
    if (!conversation) {
        return;
    }

    state.draftByConversation[conversation.id] = event.target.value;
    autoResizeComposer();
    renderConversationList();
    renderChatHeader();
    elements.typingState.textContent = typingStatusText(conversation);
    elements.uploadState.textContent = uploadStatusText();
}

async function handleSendMessage(event) {
    if (event) {
        event.preventDefault();
    }

    const conversation = activeConversation();
    if (!conversation) {
        pushToast('Pilih percakapan terlebih dahulu.', 'error');
        return;
    }

    const text = (state.draftByConversation[conversation.id] || '').trim();
    if (!text) {
        return;
    }

    const sendGuardMessage = conversationSendGuard(conversation);
    if (sendGuardMessage) {
        pushToast(sendGuardMessage, sendGuardMessage.includes('sedang disiapkan') ? 'info' : 'error');
        return;
    }

    try {
        if (conversation.type === 'room') {
            sendSocketEvent('room.message.send', {
                conversationId: conversation.id,
                text
            });
        } else {
            sendSocketEvent('dm.message.send', {
                targetSessionId: conversation.targetSessionId,
                text
            });
        }
        state.draftByConversation[conversation.id] = '';
        renderConversationList();
        renderChatHeader();
        renderComposerState();
    } catch (error) {
        pushToast(error.message, 'error');
    }
}

async function handleFileSelection(event) {
    const file = event.target.files?.[0];
    elements.fileInput.value = '';
    if (!file) {
        return;
    }

    const conversation = activeConversation();
    if (!conversation) {
        pushToast('Pilih percakapan sebelum mengirim file.', 'error');
        return;
    }

    const sendGuardMessage = conversationSendGuard(conversation, { isFile: true });
    if (sendGuardMessage) {
        pushToast(sendGuardMessage, sendGuardMessage.includes('sedang disiapkan') ? 'info' : 'error');
        return;
    }

    const validationError = validateFile(file);
    if (validationError) {
        pushToast(validationError, 'error');
        return;
    }

    try {
        state.upload = { status: 'uploading', fileName: file.name };
        renderComposerState();

        const formData = new FormData();
        formData.append('file', file);
        const upload = await fetchJson('/api/files', {
            method: 'POST',
            body: formData
        });

        const text = (state.draftByConversation[conversation.id] || '').trim();
        if (conversation.type === 'room') {
            sendSocketEvent('room.message.send', {
                conversationId: conversation.id,
                text: text || undefined,
                fileId: upload.fileId
            });
        } else {
            sendSocketEvent('dm.message.send', {
                targetSessionId: conversation.targetSessionId,
                text: text || undefined,
                fileId: upload.fileId
            });
        }

        state.draftByConversation[conversation.id] = '';
        state.upload = { status: 'done', fileName: file.name };
        renderConversationList();
        renderChatHeader();
        renderComposerState();
        pushToast('File berhasil dikirim.', 'success');
        window.setTimeout(() => {
            state.upload = { status: 'idle', fileName: '' };
            renderComposerState();
        }, 1600);
    } catch (error) {
        state.upload = { status: 'error', fileName: file.name };
        renderComposerState();
        pushToast(error.message, 'error');
    }
}

function validateFile(file) {
    if (!file) {
        return 'File tidak ditemukan.';
    }
    if (file.size > MAX_FILE_SIZE_BYTES) {
        return 'File terlalu besar. Maksimal 10 MB.';
    }
    if (isAllowedFile(file)) {
        return '';
    }
    return 'Format file tidak didukung. Gunakan gambar, PDF, TXT, atau ZIP.';
}

function isAllowedFile(file) {
    const name = file.name.toLowerCase();
    if (file.type && file.type.startsWith('image/')) {
        return true;
    }
    return (
        name.endsWith('.pdf') ||
        name.endsWith('.txt') ||
        name.endsWith('.zip') ||
        name.endsWith('.png') ||
        name.endsWith('.jpg') ||
        name.endsWith('.jpeg') ||
        name.endsWith('.gif') ||
        name.endsWith('.webp') ||
        name.endsWith('.bmp') ||
        name.endsWith('.svg')
    );
}

function sendSocketEvent(type, payload) {
    if (!state.socket || state.socket.readyState !== WebSocket.OPEN) {
        throw new Error(connectionFeedbackMessage());
    }

    state.socket.send(JSON.stringify({ type, payload }));
}

function connectionFeedbackMessage() {
    if (!state.session) {
        return 'Masuk dulu untuk mulai chat.';
    }
    if (!state.connected) {
        return 'Koneksi terputus. Tunggu hingga server realtime tersambung kembali.';
    }
    return 'Percakapan belum siap.';
}

async function handleCreateRoom() {
    if (!state.session) {
        pushToast('Masuk dulu sebelum membuat room baru.', 'error');
        return;
    }

    const roomName = window.prompt('Nama room baru', '');
    if (roomName === null) {
        return;
    }

    const trimmedName = roomName.trim();
    if (!trimmedName) {
        pushToast('Nama room wajib diisi.', 'error');
        return;
    }

    try {
        const room = await postJson('/api/rooms', { name: trimmedName });
        mergeRoom(room);
        pushToast(`Room "${room.name}" berhasil dibuat.`, 'success');
        selectConversation(room.roomId, 'room');
    } catch (error) {
        pushToast(error.message, 'error');
    }
}

function handleViewportResize() {
    if (window.innerWidth > 960) {
        state.mobileView = 'chat';
    } else if (!state.activeConversationId) {
        state.mobileView = 'list';
    }
    renderSessionState();
}

function activeConversation() {
    return buildConversations().find((conversation) => {
        return conversation.id === state.activeConversationId && conversation.type === state.activeConversationType;
    }) || null;
}

function createEmptyListState() {
    const container = document.createElement('div');
    container.className = 'empty-state';
    const description = state.searchQuery
        ? 'Tidak ada room atau orang yang cocok dengan kata kunci pencarian Anda.'
        : state.filter === 'room'
            ? 'Belum ada room yang tersedia. Buat room baru untuk memulai obrolan grup.'
            : state.filter === 'dm'
                ? 'Belum ada pengguna aktif lain untuk memulai percakapan langsung.'
                : 'Belum ada room atau pengguna aktif yang tersedia saat ini.';
    container.append(
        createEmptyAvatar('?', false),
        createHeading('Tidak ada percakapan'),
        createParagraph(description)
    );
    return container;
}

function createEmptyChatState(title, description) {
    const container = document.createElement('div');
    container.className = 'empty-state';
    container.append(
        createEmptyAvatar('#', true),
        createHeading(title),
        createParagraph(description)
    );
    return container;
}

function createEmptyHeader() {
    const main = document.createElement('div');
    main.className = 'chat-header__main';
    const avatar = createEmptyAvatar('#', true);
    const text = document.createElement('div');
    text.className = 'chat-header__text';
    const titleRow = document.createElement('div');
    titleRow.className = 'chat-title';
    const title = document.createElement('h3');
    title.textContent = 'Pilih percakapan';
    titleRow.appendChild(title);
    const subtitle = document.createElement('div');
    subtitle.className = 'chat-subtitle';
    subtitle.textContent = 'Pilih room atau percakapan langsung dari panel kiri untuk mulai chat.';
    text.append(titleRow, subtitle);
    main.append(avatar, text);
    return [main, document.createElement('div')];
}

function createEmptyAvatar(content, room) {
    const avatar = document.createElement('div');
    avatar.className = `empty-avatar ${room ? 'empty-avatar--room' : ''}`;
    avatar.textContent = content;
    return avatar;
}

function createHeading(text) {
    const heading = document.createElement('h3');
    heading.textContent = text;
    return heading;
}

function createParagraph(text) {
    const paragraph = document.createElement('p');
    paragraph.textContent = text;
    return paragraph;
}

function setConnectionMode(mode) {
    state.connectionMode = mode;
    renderConnectionStrip();
    renderComposerState();
}

function canCompose() {
    return Boolean(state.session && state.connected && state.activeConversationId && state.upload.status !== 'uploading');
}

function conversationSendGuard(conversation, options = {}) {
    if (!state.session) {
        return 'Masuk dulu untuk mulai chat.';
    }
    if (!state.connected) {
        return connectionFeedbackMessage();
    }
    if (!conversation) {
        return 'Pilih percakapan terlebih dahulu.';
    }
    if (conversation.type === 'room' && !state.joinedRooms[conversation.id]) {
        ensureRoomJoined(conversation.id);
        return options.isFile
            ? 'Room sedang disiapkan. Coba kirim file lagi dalam sesaat.'
            : 'Room sedang disiapkan. Coba kirim pesan lagi dalam sesaat.';
    }
    if (conversation.type === 'dm' && conversation.targetSessionId && !conversation.online) {
        return options.isFile
            ? 'Pengguna tujuan sedang tidak aktif. File baru bisa dikirim saat mereka online.'
            : 'Pengguna tujuan sedang tidak aktif. Pesan baru bisa dikirim saat mereka online.';
    }
    return '';
}

function autoResizeComposer() {
    elements.messageInput.style.height = 'auto';
    elements.messageInput.style.height = `${Math.min(elements.messageInput.scrollHeight, 120)}px`;
}

function scrollTimelineToBottom() {
    window.requestAnimationFrame(() => {
        elements.messageTimeline.scrollTop = elements.messageTimeline.scrollHeight;
    });
}

function initials(value) {
    const source = (value || '').trim();
    if (!source) {
        return '--';
    }

    return source
        .split(/\s+/)
        .slice(0, 2)
        .map((part) => part.charAt(0).toUpperCase())
        .join('');
}

function parseTimestamp(value) {
    if (!value) {
        return 0;
    }
    const timestamp = new Date(value).getTime();
    return Number.isNaN(timestamp) ? 0 : timestamp;
}

function formatTime(value) {
    const timestamp = parseTimestamp(value);
    if (!timestamp) {
        return '--:--';
    }
    return timeFormatter.format(new Date(timestamp));
}

function formatDay(value) {
    const timestamp = parseTimestamp(value);
    if (!timestamp) {
        return 'Hari ini';
    }
    return dayFormatter.format(new Date(timestamp));
}

function formatFileSize(sizeBytes) {
    if (!sizeBytes && sizeBytes !== 0) {
        return '--';
    }
    if (sizeBytes < 1024) {
        return `${sizeBytes} B`;
    }
    if (sizeBytes < 1024 * 1024) {
        return `${(sizeBytes / 1024).toFixed(1)} KB`;
    }
    return `${(sizeBytes / (1024 * 1024)).toFixed(1)} MB`;
}

function friendlyFileType(contentType) {
    if (!contentType) {
        return 'File';
    }
    if (contentType.startsWith('image/')) {
        return 'Gambar';
    }
    if (contentType === 'application/pdf') {
        return 'PDF';
    }
    if (contentType === 'text/plain') {
        return 'Teks';
    }
    if (contentType.includes('zip')) {
        return 'ZIP';
    }
    return contentType;
}

function isImageFile(contentType) {
    return Boolean(contentType && contentType.startsWith('image/'));
}

function directConversationId(firstSessionId, secondSessionId) {
    return firstSessionId <= secondSessionId
        ? `dm:${firstSessionId}:${secondSessionId}`
        : `dm:${secondSessionId}:${firstSessionId}`;
}

function otherDirectParticipant(conversationId) {
    if (!conversationId || !state.session) {
        return '';
    }

    const [, first, second] = conversationId.split(':');
    if (first === state.session.sessionId) {
        return second;
    }
    if (second === state.session.sessionId) {
        return first;
    }
    return first || second || '';
}

function resolveDirectTargetSessionId(message) {
    if (!state.session) {
        return '';
    }
    if (isCurrentUserMessage(message)) {
        return message.targetSessionId || otherDirectParticipant(message.conversationId);
    }
    return message.senderSessionId;
}

function isCurrentUserMessage(message) {
    if (!state.session || !message) {
        return false;
    }
    if (message.senderSessionId === state.session.sessionId) {
        return true;
    }
    return normalizeIdentityName(message.senderName) === normalizeIdentityName(state.session.username);
}

function normalizeIdentityName(value) {
    return String(value || '').trim().toLocaleLowerCase('id');
}

async function postJson(url, payload) {
    return fetchJson(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    const contentType = response.headers.get('Content-Type') || '';
    const body = contentType.includes('application/json')
        ? await response.json()
        : null;

    if (!response.ok) {
        const error = new Error(body?.error?.message || 'Permintaan ke server gagal.');
        error.code = body?.error?.code || 'request_failed';
        error.data = body?.error || {};
        throw error;
    }

    return body;
}
