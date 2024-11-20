package com.cloudComputing.collaborativeNotes.database.seeders;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteVersion;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteVersionRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Seeder class for NoteVersion entity
@Component
public class NoteVersionSeeder {

    private final NoteVersionRepository noteVersionRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteVersionSeeder(NoteVersionRepository noteVersionRepository, NoteRepository noteRepository, UserRepository userRepository) {
        this.noteVersionRepository = noteVersionRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    // Method to seed the NoteVersion table
    public void seed() {
        if (noteVersionRepository.count() == 0) { // Check if NoteVersion table is empty
            Note note = noteRepository.findById(1L).orElseThrow(() -> new RuntimeException("Note not found"));
            User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));

            NoteVersion noteVersion = new NoteVersion();
            noteVersion.setNote(note);
            noteVersion.setUser(user);
            noteVersion.setVersionNumber(1);
            noteVersion.setContent("Version 1 of the note");
            noteVersion.setCreatedAt(LocalDateTime.now());
            noteVersionRepository.save(noteVersion); // Save the note version to the database
        }
    }
}

