package com.cloudComputing.collaborativeNotes.database.seeders;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteChangeRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import org.springframework.stereotype.Component;

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
            noteChange.setContent("Initial content change");
            noteChange.setChangeType(NoteChange.ChangeType.ADDED);

            // Save the note change to the database
            noteChangeRepository.save(noteChange);

            // Update the note content to reflect the latest change
            note.setContent(noteChange.getContent());
            noteRepository.save(note);
        }
    }
}
