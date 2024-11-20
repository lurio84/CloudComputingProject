package com.cloudComputing.collaborativeNotes.database.seeders;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteChangeRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Seeder class for NoteChange entity
@Component
public class NoteChangeSeeder {

    private final NoteChangeRepository noteChangeRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteChangeSeeder(NoteChangeRepository noteChangeRepository, NoteRepository noteRepository, UserRepository userRepository) {
        this.noteChangeRepository = noteChangeRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    // Method to seed the NoteChange table
    public void seed() {
        if (noteChangeRepository.count() == 0) { // Check if NoteChange table is empty
            Note note = noteRepository.findById(1L).orElseThrow(() -> new RuntimeException("Note not found"));
            User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));

            NoteChange noteChange = new NoteChange();
            noteChange.setNote(note);
            noteChange.setUser(user);
            noteChange.setTimestamp(LocalDateTime.now());
            noteChange.setContent("Initial content change");
            noteChange.setChangeType("edit");
            noteChangeRepository.save(noteChange); // Save the note change to the database
        }
    }
}