let stompClient = null;
let originalContent = "";
let localUserId = null;

function connect() {
    const socket = new SockJS("http://localhost:8080/ws"); // Usar SockJS en lugar de WebSocket

    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000, // Intentar reconectar en caso de desconexión
        onConnect: (frame) => {
            console.log("Connected: " + frame);

            // Suscribirse a las actualizaciones en tiempo real
            stompClient.subscribe("/topic/notes/1", function (message) {
                const noteEditMessage = JSON.parse(message.body);
                console.log("Message received: ", noteEditMessage);

                // Aplicar el diff solo si el mensaje no es del usuario local
                if (noteEditMessage.userId !== localUserId) {
                    showMessage(noteEditMessage);
                    applyReceivedDiff(noteEditMessage.diff);
                } else {
                    console.log("Skipping diff application for local user.");
                }
            });
        },
        onStompError: (frame) => {
            console.error("STOMP error: ", frame);
        }
    });

    stompClient.activate(); // Activar el cliente STOMP
}

function fetchInitialNoteContent(noteId) {
    fetch(`http://localhost:8080/notes/${noteId}`)
        .then(response => response.json())
        .then(note => {
            document.getElementById("noteContent").value = note.content;
            originalContent = note.content;
            connect(); // Conectar después de cargar el contenido
        })
        .catch(error => console.error("Error fetching initial note content:", error));
}

function generateAndSendDiff() {
    if (!stompClient || !stompClient.connected) {
        console.error("STOMP client is not connected. Message not sent.");
        return;
    }

    const textArea = document.getElementById("noteContent");
    const currentContent = textArea.value;
    const userId = parseInt(document.getElementById("userId").value);

    localUserId = userId;

    // Capturar la posición del cursor antes de generar la diff
    const cursorPosition = textArea.selectionStart;

    const dmp = new diff_match_patch();
    const diffs = dmp.diff_main(originalContent, currentContent);
    dmp.diff_cleanupSemantic(diffs);
    const patch = dmp.patch_toText(dmp.patch_make(diffs));

    const message = {
        noteId: 1,
        userId: userId,
        diff: patch
    };

    stompClient.publish({
        destination: "/app/edit",
        body: JSON.stringify(message)
    });

    console.log("Diff sent: ", message);

    originalContent = currentContent;

    // Restaurar la posición del cursor después de enviar el diff
    textArea.setSelectionRange(cursorPosition, cursorPosition);
}

function applyReceivedDiff(diff) {
    const textArea = document.getElementById("noteContent");
    const dmp = new diff_match_patch();

    // Guardar la posición del cursor
    const cursorPosition = textArea.selectionStart;

    // Obtener contenido actual dividido por líneas
    const currentContentLines = textArea.value.split("\n");

    // Identificar la línea y la posición relativa del cursor
    let cursorLine = 0;
    let cursorOffset = cursorPosition;
    for (let i = 0; i < currentContentLines.length; i++) {
        if (cursorOffset <= currentContentLines[i].length) {
            cursorLine = i;
            break;
        }
        cursorOffset -= currentContentLines[i].length + 1;
    }

    // Aplicar la diff al contenido actual
    const patches = dmp.patch_fromText(diff);
    const [updatedContent, results] = dmp.patch_apply(patches, textArea.value);

    if (textArea.value !== updatedContent) {
        textArea.value = updatedContent;
        originalContent = updatedContent;

        // Calcular nueva posición del cursor
        const updatedContentLines = updatedContent.split("\n");

        let adjustedCursorPosition = 0;
        let hasCursorMoved = false;

        for (let i = 0; i < updatedContentLines.length; i++) {
            if (i < cursorLine) {
                adjustedCursorPosition += updatedContentLines[i].length + 1;
            } else if (i === cursorLine) {
                const originalLineLength = currentContentLines[i]?.length || 0;
                const updatedLineLength = updatedContentLines[i]?.length || 0;

                const changeInLine = updatedLineLength - originalLineLength;

                if (cursorOffset <= originalLineLength) {
                    adjustedCursorPosition += cursorOffset + Math.max(0, changeInLine);
                } else {
                    adjustedCursorPosition += cursorOffset;
                }
                hasCursorMoved = true;
                break;
            }
        }

        if (!hasCursorMoved) {
            for (let i = cursorLine + 1; i < updatedContentLines.length; i++) {
                adjustedCursorPosition += updatedContentLines[i].length + 1;
            }
        }

        // Restaurar la posición del cursor
        textArea.setSelectionRange(adjustedCursorPosition, adjustedCursorPosition);
    }
}

function showMessage(message) {
    const messagesDiv = document.getElementById("messages");
    const newMessage = document.createElement("p");
    newMessage.textContent = `User ${message.userId}: ${message.diff}`;
    newMessage.style.backgroundColor = "#e8f5e9";
    newMessage.style.padding = "5px";
    newMessage.style.borderRadius = "5px";
    messagesDiv.appendChild(newMessage);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

// Enviar diff en cada cambio
document.getElementById("noteContent").addEventListener("input", generateAndSendDiff);

// Cargar contenido inicial y conectar
window.onload = () => fetchInitialNoteContent(1);
