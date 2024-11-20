package com.cloudComputing.collaborativeNotes.database.seeders;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Seeder class for Note entity
@Component
public class NoteSeeder {

    private final NoteRepository noteRepository;

    public NoteSeeder(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    // Method to seed the Note table
    public void seed() {
        if (noteRepository.count() == 0) { // Check if Note table is empty
            Note note = new Note();
            note.setTitle("First Note");
            note.setContent("This is the content of the first note.");
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());
            noteRepository.save(note); // Save the note to the database
        }
    }
}
