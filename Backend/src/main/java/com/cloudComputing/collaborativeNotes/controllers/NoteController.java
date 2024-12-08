package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteChangeRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import com.cloudComputing.collaborativeNotes.models.DiffRequest;
import com.cloudComputing.collaborativeNotes.services.NoteVersionService;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;

@RestController
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteChangeRepository noteChangeRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final DiffMatchPatch dmp = new DiffMatchPatch();

    @MessageMapping("/edit")
    public void handleEdit(DiffRequest diffRequest) {
        System.out.println("Received DiffRequest: " + diffRequest);

        // Validate the DiffRequest
        if (diffRequest == null || diffRequest.getDiff() == null || diffRequest.getDiff().isEmpty()) {
            throw new IllegalArgumentException("Invalid DiffRequest");
        }

        // Retrieve the note and the user
        Optional<Note> optionalNote = noteRepository.findById(diffRequest.getNoteId());
        Optional<User> optionalUser = userRepository.findById(diffRequest.getUserId());
        if (optionalNote.isEmpty() || optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Note or User not found");
        }

        Note note = optionalNote.get();
        User user = optionalUser.get();
        String originalContent = note.getContent();

        try {
            // Apply the received diff to the original content
            String updatedContent = applyDiffToContent(diffRequest.getDiff(), originalContent);
            note.setContent(updatedContent);

            // Determine the type of change based on the content of the diff
            NoteChange.ChangeType changeType = determineChangeType(diffRequest.getDiff());

            // Save the updated note to the database
            noteRepository.save(note);

            // Create a new NoteChange to save the changes
            NoteChange noteChange = new NoteChange();
            noteChange.setNote(note);
            noteChange.setUser(user);
            noteChange.setChangeType(changeType);
            noteChange.setDiff(diffRequest.getDiff());
            noteChange.setTimestamp(LocalDateTime.now());
            noteChangeRepository.save(noteChange);

            // Prepare the message to send to other clients
            DiffRequest messageForOthers = new DiffRequest();
            messageForOthers.setNoteId(diffRequest.getNoteId());
            messageForOthers.setUserId(diffRequest.getUserId()); // Include the user who made the change
            messageForOthers.setDiff(diffRequest.getDiff());

            // Send the diff to other clients
            messagingTemplate.convertAndSend("/topic/notes/" + note.getId(), messageForOthers);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to apply diff");
        }
    }

    // Method to apply the received diff to the original content
    private String applyDiffToContent(String diff, String originalContent) {
        LinkedList<DiffMatchPatch.Patch> patches = (LinkedList<DiffMatchPatch.Patch>) dmp.patchFromText(diff);
        Object[] result = dmp.patchApply(patches, originalContent);
        return (String) result[0];
    }

    // Method to determine the type of change based on the content of the diff
    private NoteChange.ChangeType determineChangeType(String diff) {
        LinkedList<DiffMatchPatch.Patch> patches = (LinkedList<DiffMatchPatch.Patch>) dmp.patchFromText(diff);
        for (DiffMatchPatch.Patch patch : patches) {
            for (DiffMatchPatch.Diff diffItem : patch.diffs) {
                if (diffItem.operation == DiffMatchPatch.Operation.INSERT) {
                    return NoteChange.ChangeType.ADDED;
                } else if (diffItem.operation == DiffMatchPatch.Operation.DELETE) {
                    return NoteChange.ChangeType.DELETED;
                }
            }
        }
        return NoteChange.ChangeType.EDITED;
    }

    // Endpoint to retrieve the current content of a specific note
    @GetMapping("/notes/{noteId}")
    public ResponseEntity<Note> getNoteContent(@PathVariable Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        return ResponseEntity.ok(note);
    }

    @Autowired
    private NoteVersionService noteVersionService;

    @PostMapping("/{noteId}/version")
    public void createNoteVersion(@PathVariable Long noteId, @RequestParam Long userId) {
        noteVersionService.saveNoteVersion(noteId, userId);
    }
}
