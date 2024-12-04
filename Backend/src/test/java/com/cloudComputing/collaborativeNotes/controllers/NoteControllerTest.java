package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.controllers.NoteController;
import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class NoteControllerTest {

    @InjectMocks
    private NoteController noteController;

    @Mock
    private NoteRepository noteRepository;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetNoteContent() {
        // Arrange
        Long noteId = 1L;
        Note mockNote = new Note();
        mockNote.setId(noteId);
        mockNote.setTitle("Test Note");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(mockNote));

        // Act
        ResponseEntity<Note> response = noteController.getNoteContent(noteId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockNote, response.getBody());
    }
}
