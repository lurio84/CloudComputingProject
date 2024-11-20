package com.cloudComputing.collaborativeNotes.database.seeders;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.entities.UserNote;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserNoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Seeder class for UserNote entity
@Component
public class UserNoteSeeder {

    private final UserNoteRepository userNoteRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    public UserNoteSeeder(UserNoteRepository userNoteRepository, UserRepository userRepository, NoteRepository noteRepository) {
        this.userNoteRepository = userNoteRepository;
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
    }

    // Method to seed the UserNote table
    public void seed() {
        if (userNoteRepository.count() == 0) { // Check if UserNote table is empty
            User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
            Note note = noteRepository.findById(1L).orElseThrow(() -> new RuntimeException("Note not found"));

            UserNote userNote = new UserNote();
            userNote.setUser(user);
            userNote.setNote(note);
            userNote.setAccessLevel("editor");
            userNote.setAddedAt(LocalDateTime.now());
            userNoteRepository.save(userNote); // Save the user-note relationship to the database
        }
    }
}
