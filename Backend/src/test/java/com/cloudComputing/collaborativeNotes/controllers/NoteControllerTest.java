package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteChangeRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import com.cloudComputing.collaborativeNotes.models.DiffRequest;
import com.cloudComputing.collaborativeNotes.services.NoteVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class NoteControllerTest {

    @InjectMocks
    private NoteController noteController;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NoteChangeRepository noteChangeRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NoteVersionService noteVersionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleEditSuccess() {
        // Arrange
        Long noteId = 1L;
        Long userId = 2L;
        String diff = "@@ -1,12 +1,13 @@\n Hello Worl\n+d\n"; // Diff válido para DiffMatchPatch
        DiffRequest diffRequest = new DiffRequest();
        diffRequest.setNoteId(noteId);
        diffRequest.setUserId(userId);
        diffRequest.setDiff(diff);

        Note mockNote = new Note();
        mockNote.setId(noteId);
        mockNote.setContent("Hello Worl"); // Contenido original esperado

        User mockUser = new User();
        mockUser.setId(userId);

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(mockNote));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        noteController.handleEdit(diffRequest);

        // Assert
        verify(noteRepository).save(mockNote);
        verify(noteChangeRepository).save(any(NoteChange.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/notes/" + noteId), any(DiffRequest.class));

        // Verificar que el contenido se actualizó correctamente
        assertEquals("Hello World", mockNote.getContent());
    }

    @Test
    void testHandleEditNoteNotFound() {
        // Arrange
        Long noteId = 1L;
        DiffRequest diffRequest = new DiffRequest();
        diffRequest.setNoteId(noteId);
        diffRequest.setUserId(2L);
        diffRequest.setDiff("@@ -1,12 +1,13 @@\n Hello Worl\n+d\n");

        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> noteController.handleEdit(diffRequest));
    }

    @Test
    void testApplyDiffToContent() {
        // Arrange
        String originalContent = "Hello Worl";
        String diff = "@@ -1,12 +1,13 @@\n Hello Worl\n+d\n";

        // Act
        String updatedContent = noteController.applyDiffToContent(diff, originalContent);

        // Assert
        assertEquals("Hello World", updatedContent);
    }

    @Test
    void testDetermineChangeTypeAdded() {
        // Arrange
        String diff = "@@ -1,12 +1,13 @@\n Hello Worl\n+d\n";

        // Act
        NoteChange.ChangeType changeType = noteController.determineChangeType(diff);

        // Assert
        assertEquals(NoteChange.ChangeType.ADDED, changeType);
    }

    @Test
    void testGetNoteContentSuccess() {
        // Arrange
        Long noteId = 1L;
        Note mockNote = new Note();
        mockNote.setId(noteId);
        mockNote.setContent("Sample content");
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(mockNote));

        // Act
        ResponseEntity<Note> response = noteController.getNoteContent(noteId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockNote, response.getBody());
    }

    @Test
    void testCreateNoteVersion() {
        // Arrange
        Long noteId = 1L;
        Long userId = 2L;

        // Act
        noteController.createNoteVersion(noteId, userId);

        // Assert
        verify(noteVersionService).saveNoteVersion(noteId, userId);
    }
}
