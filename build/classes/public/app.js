const statusNode = document.getElementById('status');
const messagesNode = document.getElementById('messages');
const nameInput = document.getElementById('nameInput');
const messageInput = document.getElementById('messageInput');
const connectButton = document.getElementById('connectButton');
const sendButton = document.getElementById('sendButton');

let socket;

function setStatus(text) {
    statusNode.textContent = text;
}

function appendMessage(text, type = 'user') {
    const item = document.createElement('div');
    item.className = `message ${type}`;
    item.textContent = text;
    messagesNode.appendChild(item);
    messagesNode.scrollTop = messagesNode.scrollHeight;
}

function updateComposerState(enabled) {
    messageInput.disabled = !enabled;
    sendButton.disabled = !enabled;
}

function connect() {
    if (socket && socket.readyState === WebSocket.OPEN) {
        setStatus('Koneksi sudah aktif.');
        return;
    }

    const name = nameInput.value.trim() || 'User';
    socket = new WebSocket(`ws://localhost:8887/chat?name=${encodeURIComponent(name)}`);

    setStatus('Mencoba terhubung ke server...');

    socket.addEventListener('open', () => {
        setStatus(`Terhubung sebagai ${name}.`);
        updateComposerState(true);
        appendMessage('Koneksi berhasil dibuat.', 'server');
        messageInput.focus();
    });

    socket.addEventListener('message', (event) => {
        const type = event.data.includes('server:') ? 'server' : 'user';
        appendMessage(event.data, type);
    });

    socket.addEventListener('close', () => {
        setStatus('Koneksi ditutup.');
        updateComposerState(false);
        appendMessage('Koneksi terputus.', 'server');
    });

    socket.addEventListener('error', () => {
        setStatus('Terjadi error saat menghubungkan WebSocket.');
        updateComposerState(false);
        appendMessage('Server belum aktif atau koneksi gagal.', 'server');
    });
}

function sendMessage() {
    const message = messageInput.value.trim();
    if (!socket || socket.readyState !== WebSocket.OPEN || !message) {
        return;
    }

    socket.send(message);
    messageInput.value = '';
    messageInput.focus();
}

connectButton.addEventListener('click', connect);
sendButton.addEventListener('click', sendMessage);
messageInput.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
        sendMessage();
    }
});
