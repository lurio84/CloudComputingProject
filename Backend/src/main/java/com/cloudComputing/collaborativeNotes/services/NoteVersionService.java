package com.cloudComputing.collaborativeNotes.services;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteVersion;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteVersionRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NoteVersionService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteVersionRepository noteVersionRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveNoteVersion(Long noteId, Long userId) {
        // Find the note by its ID
        Optional<Note> optionalNote = noteRepository.findById(noteId);
        // Find the user by their ID
        Optional<User> optionalUser = userRepository.findById(userId);

        // If both note and user exist, proceed to save the note version
        if (optionalNote.isPresent() && optionalUser.isPresent()) {
            Note note = optionalNote.get();
            User user = optionalUser.get();
            String currentContent = note.getContent();

            // Get the most recent version number
            int latestVersionNumber = noteVersionRepository.findLatestVersionNumberByNoteId(noteId).orElse(0);

            // Create a new version of the note
            NoteVersion newVersion = new NoteVersion();
            newVersion.setNote(note);
            newVersion.setUser(user);
            newVersion.setVersionNumber(latestVersionNumber + 1);
            newVersion.setContent(currentContent);
            newVersion.setCreatedAt(LocalDateTime.now());

            // Save the new version
            noteVersionRepository.save(newVersion);
        } else {
            // Throw an exception if the note or user is not found
            throw new IllegalArgumentException("Note with ID " + noteId + " or User with ID " + userId + " not found");
        }
    }
}
