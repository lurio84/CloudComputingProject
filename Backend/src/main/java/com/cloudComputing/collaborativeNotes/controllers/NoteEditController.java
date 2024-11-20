package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteChangeRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import com.cloudComputing.collaborativeNotes.models.NoteEditMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

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
        // Buscar la nota correspondiente a partir del noteId
        Note note = noteRepository.findById(message.getNoteId())
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Buscar el usuario correspondiente a partir del userId
        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Crear una nueva instancia de NoteChange
        NoteChange noteChange = new NoteChange();
        noteChange.setNote(note);
        noteChange.setUser(user);
        noteChange.setContent(message.getContent());
        noteChange.setTimestamp(LocalDateTime.now());
        noteChange.setChangeType(NoteChange.ChangeType.EDITED);

        // Guardar el cambio en la base de datos
        noteChangeRepository.save(noteChange);

        // Actualizar el contenido de la nota
        note.setContent(message.getContent());
        noteRepository.save(note);

        // Retransmitir el mensaje a todos los clientes suscritos al documento
        return message;
    }
}

