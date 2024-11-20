package com.cloudComputing.collaborativeNotes.services;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.models.NoteEditMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteService {
    @Autowired
    private NoteRepository noteRepository;

    public void applyChangeToNote(NoteEditMessage message) {
        Note note = noteRepository.findById(message.getNoteId()).orElseThrow(() -> new RuntimeException("Note not found"));
        note.setContent(message.getContent());
        noteRepository.save(note);
    }
}
