let stompClient = null;
let originalContent = "";
let localUserId = null;

function connect() {
    const socket = new WebSocket("ws://localhost:8080/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {}; // Disable debug logs

    stompClient.connect({}, function (frame) {
        console.log("Connected: " + frame);

        // Subscribe to updates
        stompClient.subscribe("/topic/notes/1", function (message) {
            const noteEditMessage = JSON.parse(message.body);
            console.log("Message received: ", noteEditMessage);

            // Apply the received diff if it is not from the local user
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
    fetch(`http://localhost:8080/notes/${noteId}`)
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

    // Capture the current cursor position before generating the diff
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

    // Restore the cursor position after sending the diff
    textArea.setSelectionRange(cursorPosition, cursorPosition);
}

function applyReceivedDiff(diff) {
    const textArea = document.getElementById("noteContent");
    const dmp = new diff_match_patch();

    // Save the current cursor position
    const cursorPosition = textArea.selectionStart;

    // Get the current content divided by lines
    const currentContentLines = textArea.value.split("\n");

    // Identify the line and relative position of the cursor
    let cursorLine = 0;
    let cursorOffset = cursorPosition;
    for (let i = 0; i < currentContentLines.length; i++) {
        if (cursorOffset <= currentContentLines[i].length) {
            cursorLine = i;
            break;
        }
        cursorOffset -= currentContentLines[i].length + 1; // +1 for the newline character
    }

    // Apply the diff to the current content
    const patches = dmp.patch_fromText(diff);
    const [updatedContent, results] = dmp.patch_apply(patches, textArea.value);

    if (textArea.value !== updatedContent) {
        // Update the textarea content
        textArea.value = updatedContent;
        originalContent = updatedContent;

        // Calculate the new cursor position
        const updatedContentLines = updatedContent.split("\n");

        let adjustedCursorPosition = 0;
        let hasCursorMoved = false;

        for (let i = 0; i < updatedContentLines.length; i++) {
            if (i < cursorLine) {
                // Before the current line, add full lengths
                adjustedCursorPosition += updatedContentLines[i].length + 1;
            } else if (i === cursorLine) {
                // On the same line, adjust according to the change in that line
                const originalLineLength = currentContentLines[i]?.length || 0;
                const updatedLineLength = updatedContentLines[i]?.length || 0;

                const changeInLine = updatedLineLength - originalLineLength;

                if (cursorOffset <= originalLineLength) {
                    // If the change is before the cursor, adjust proportionally
                    adjustedCursorPosition += cursorOffset + Math.max(0, changeInLine);
                } else {
                    // If the change is after the cursor in the same line, do not move the cursor
                    adjustedCursorPosition += cursorOffset;
                }
                hasCursorMoved = true;
                break;
            }
        }

        // If the cursor has not moved, add the rest of the content
        if (!hasCursorMoved) {
            for (let i = cursorLine + 1; i < updatedContentLines.length; i++) {
                adjustedCursorPosition += updatedContentLines[i].length + 1;
            }
        }

        // Restore the cursor to the new adjusted position
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

// Send the diff on every character change
document.getElementById("noteContent").addEventListener("input", generateAndSendDiff);

window.onload = () => fetchInitialNoteContent(1);
