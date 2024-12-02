let stompClient = null;
let originalContent = "";
let localUserId = null;

function connect() {
    const socket = new WebSocket("ws://25.12.181.24:8080/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {}; // Disable debug logs

    stompClient.connect({}, function (frame) {
        console.log("Connected: " + frame);

        // Suscribirse a las actualizaciones
        stompClient.subscribe("/topic/notes/1", function (message) {
            const noteEditMessage = JSON.parse(message.body);
            console.log("Message received: ", noteEditMessage);

            // Aplicar el diff recibido si no es del usuario local
            if (noteEditMessage.userId !== localUserId) {
                showMessage(noteEditMessage);
                applyReceivedDiff(noteEditMessage.diff);
            } else {
                console.log("Skipping diff application for local user.");
            }
        });
    });
}

function fetchInitialNoteContent(noteId) {
    fetch(`http://25.12.181.24:8080/notes/${noteId}`)
        .then(response => response.json())
        .then(note => {
            document.getElementById("noteContent").value = note.content;
            originalContent = note.content;
            connect();
        })
        .catch(error => console.error("Error fetching initial note content:", error));
}

function generateAndSendDiff() {
    const textArea = document.getElementById("noteContent");
    const currentContent = textArea.value;
    const userId = parseInt(document.getElementById("userId").value);

    localUserId = userId;

    // Capturar la posición actual del cursor antes de generar el diff
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

    stompClient.send("/app/edit", {}, JSON.stringify(message));
    console.log("Diff sent: ", message);

    originalContent = currentContent;

    // Restaurar el cursor después de enviar el diff
    textArea.setSelectionRange(cursorPosition, cursorPosition);
}

function applyReceivedDiff(diff) {
    const textArea = document.getElementById("noteContent");
    const dmp = new diff_match_patch();

    // Guardar la posición actual del cursor
    const cursorPosition = textArea.selectionStart;

    // Obtener el contenido actual dividido en líneas
    const currentContentLines = textArea.value.split("\n");

    // Identificar la línea y la posición relativa del cursor
    let cursorLine = 0;
    let cursorOffset = cursorPosition;
    for (let i = 0; i < currentContentLines.length; i++) {
        if (cursorOffset <= currentContentLines[i].length) {
            cursorLine = i;
            break;
        }
        cursorOffset -= currentContentLines[i].length + 1; // +1 para el salto de línea
    }

    // Aplicar el diff al contenido actual
    const patches = dmp.patch_fromText(diff);
    const [updatedContent, results] = dmp.patch_apply(patches, textArea.value);

    if (textArea.value !== updatedContent) {
        // Actualizar el contenido del textarea
        textArea.value = updatedContent;
        originalContent = updatedContent;

        // Calcular la nueva posición del cursor
        const updatedContentLines = updatedContent.split("\n");

        let adjustedCursorPosition = 0;
        let hasCursorMoved = false;

        for (let i = 0; i < updatedContentLines.length; i++) {
            if (i < cursorLine) {
                // Antes de la línea actual, sumar las longitudes completas
                adjustedCursorPosition += updatedContentLines[i].length + 1;
            } else if (i === cursorLine) {
                // En la misma línea, ajustar según el cambio en esa línea
                const originalLineLength = currentContentLines[i]?.length || 0;
                const updatedLineLength = updatedContentLines[i]?.length || 0;

                const changeInLine = updatedLineLength - originalLineLength;

                if (cursorOffset <= originalLineLength) {
                    // Si el cambio está antes del cursor, ajustar proporcionalmente
                    adjustedCursorPosition += cursorOffset + Math.max(0, changeInLine);
                } else {
                    // Si el cambio está después del cursor en la misma línea, no mover el cursor
                    adjustedCursorPosition += cursorOffset;
                }
                hasCursorMoved = true;
                break;
            }
        }

        // Si no se movió el cursor, sumar el resto del contenido
        if (!hasCursorMoved) {
            for (let i = cursorLine + 1; i < updatedContentLines.length; i++) {
                adjustedCursorPosition += updatedContentLines[i].length + 1;
            }
        }

        // Restaurar el cursor a la nueva posición ajustada
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

// Enviar el diff en cada cambio de carácter
document.getElementById("noteContent").addEventListener("input", generateAndSendDiff);

window.onload = () => fetchInitialNoteContent(1);
