package com.cloudComputing.collaborativeNotes.services;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoteVersionScheduler {

    @Autowired
    private NoteVersionService noteVersionService;

    @Autowired
    private NoteRepository noteRepository;

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void autoSaveAllNoteVersions() {
        // Get all notes from the database
        List<Note> allNotes = noteRepository.findAll();

        // Iterate through each note and save a version
        for (Note note : allNotes) {
            Long noteId = note.getId();
            // For testing purposes, you can use a fixed user ID or a user associated with the note
            Long userId = 1L; // Replace with an actual user ID if necessary
            noteVersionService.saveNoteVersion(noteId, userId);
        }
    }
}
