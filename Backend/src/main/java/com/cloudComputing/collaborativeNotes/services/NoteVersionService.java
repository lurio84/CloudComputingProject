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
        Optional<Note> optionalNote = noteRepository.findById(noteId);
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalNote.isPresent() && optionalUser.isPresent()) {
            Note note = optionalNote.get();
            User user = optionalUser.get();
            String currentContent = note.getContent();

            // Obtener el número de versión más reciente
            int latestVersionNumber = noteVersionRepository.findLatestVersionNumberByNoteId(noteId).orElse(0);

            // Crear una nueva versión de la nota
            NoteVersion newVersion = new NoteVersion();
            newVersion.setNote(note);
            newVersion.setUser(user);
            newVersion.setVersionNumber(latestVersionNumber + 1);
            newVersion.setContent(currentContent);
            newVersion.setCreatedAt(LocalDateTime.now());

            // Guardar la nueva versión
            noteVersionRepository.save(newVersion);
        } else {
            throw new IllegalArgumentException("Note with ID " + noteId + " or User with ID " + userId + " not found");
        }
    }

}
