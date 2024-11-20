package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteChangeRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import com.cloudComputing.collaborativeNotes.models.NoteEditMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;

@Controller
public class NoteEditController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteChangeRepository noteChangeRepository;

    @MessageMapping("/edit")
    @SendTo("/topic/notes/{noteId}")
    public NoteEditMessage handleEdit(NoteEditMessage message) {
        // Fetch the corresponding note based on noteId
        Note note = noteRepository.findById(message.getNoteId())
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Fetch the corresponding user based on userId
        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create a new instance of NoteChange to track the edit
        NoteChange noteChange = new NoteChange();
        noteChange.setNote(note);
        noteChange.setUser(user);
        noteChange.setContent(message.getContent());
        noteChange.setTimestamp(LocalDateTime.now());
        noteChange.setChangeType(NoteChange.ChangeType.EDITED);

        // Save the note change in the database for tracking purposes
        noteChangeRepository.save(noteChange);

        // Update the note content in the main Note entity
        note.setContent(message.getContent());
        noteRepository.save(note); // Make sure the note content is saved

        // Broadcast the message to all clients subscribed to the document
        return message;
    }

    // Endpoint to get the current content of a specific note
    @GetMapping("/notes/{noteId}")
    public ResponseEntity<Note> getNoteContent(@PathVariable Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        return ResponseEntity.ok(note);
    }

}
