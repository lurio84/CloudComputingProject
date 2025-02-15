package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.models.DiffRequest;
import com.cloudComputing.collaborativeNotes.services.NoteService;
import com.cloudComputing.collaborativeNotes.services.NoteVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteVersionService noteVersionService;

    @MessageMapping("/edit")
    public void handleEdit(@RequestBody DiffRequest diffRequest) {
        noteService.applyDiffAndNotifyClients(diffRequest);
        ResponseEntity.ok().build();
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<Note> getNoteContent(@PathVariable Long noteId) {
        try {
            Note updatedNote = noteService.getNoteWithCachedDiffs(noteId);
            if (updatedNote == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedNote);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/{noteId}/version")
    public void createNoteVersion(@PathVariable Long noteId, @RequestParam Long userId) {
        noteVersionService.saveNoteVersion(noteId, userId);
        ResponseEntity.ok().build();
    }

    // Modified to accept RequestParam
    @PostMapping
    public ResponseEntity<Note> createNote(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long userId) {
        Note newNote = new Note();
        newNote.setTitle(title);
        newNote.setContent(content);

        // It is assumed that the createNote method handles the user and other relationships
        Note createdNote = noteService.createNote(newNote, userId);
        return ResponseEntity.status(201).body(createdNote);
    }

    @PostMapping("/{noteId}/assignUser")
    public ResponseEntity<?> assignUserToNote(@PathVariable Long noteId, @RequestParam Long userId) {
        noteService.assignUserToNote(noteId, userId);
        return ResponseEntity.ok().build();
    }
}
