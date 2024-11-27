let stompClient = null;
let debounceTimer = null;

function fetchInitialNoteContent(noteId) {
    fetch(`http://localhost:8080/notes/${noteId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch note content');
            }
            return response.json();
        })
        .then(note => {
            document.getElementById("noteContent").value = note.content;
            connect();
        })
        .catch(error => {
            console.error('Error fetching initial note content:', error);
        });
}

function connect() {
    const socket = new WebSocket('ws://localhost:8080/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {};

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/notes/1', function (message) {
            const noteEditMessage = JSON.parse(message.body);
            console.log('Message received: ', noteEditMessage);
            showMessage(noteEditMessage);
        });
    }, function (error) {
        console.error('Connection error: ', error);
    });
}

function sendEdit() {
    if (stompClient && stompClient.connected) {
        const content = document.getElementById("noteContent").value;
        const userId = document.getElementById("userId").value;

        const message = {
            noteId: 1,
            userId: parseInt(userId),
            content: content
        };

        stompClient.send("/app/edit", {}, JSON.stringify(message));
        console.log('Message sent: ', message);
    } else {
        console.error('Cannot send message. WebSocket is not connected.');
    }
}

function debounceSendEdit() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => sendEdit(), 300);
}

function showMessage(message) {
    const textArea = document.getElementById("noteContent");
    if (textArea.value !== message.content) {
        textArea.value = message.content;
    }

    const messagesDiv = document.getElementById("messages");
    const newMessage = document.createElement("p");
    newMessage.appendChild(document.createTextNode("User: " + message.userId + " - Content: " + message.content));
    messagesDiv.appendChild(newMessage);
}

document.getElementById("noteContent").addEventListener("input", debounceSendEdit);

window.onload = () => fetchInitialNoteContent(1);
