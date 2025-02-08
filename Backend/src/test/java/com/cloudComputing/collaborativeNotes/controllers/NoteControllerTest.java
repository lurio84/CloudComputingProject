package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.models.DiffRequest;
import com.cloudComputing.collaborativeNotes.services.NoteService;
import com.cloudComputing.collaborativeNotes.services.NoteVersionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NoteControllerTest {

    @InjectMocks
    private NoteController noteController;

    @Mock
    private NoteService noteService;

    @Mock
    private NoteVersionService noteVersionService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
/***
    @Test
    void testHandleEditSuccess() {
        // Arrange
        DiffRequest diffRequest = createMockDiffRequest();

        // Act
        noteController.handleEdit(diffRequest);

        // Assert
        verify(noteService).applyDiffToNote(diffRequest);
        verifyNoMoreInteractions(noteService);
    }

    @Test
    void testGetNoteContentSuccess() {
        // Arrange
        Long noteId = 1L;
        Note mockNote = createMockNote(noteId);

        when(noteService.getNoteById(noteId)).thenReturn(mockNote);

        // Act
        ResponseEntity<Note> response = noteController.getNoteContent(noteId);

        // Assert
        assertEquals(200, response.getStatusCode().value(), "Response should return HTTP 200");
        assertEquals(mockNote, response.getBody(), "The body should match the mock note");
    }
 ***/

    @Test
    void testCreateNoteVersion() {
        // Arrange
        Long noteId = 1L;
        Long userId = 42L;

        // Act
        noteController.createNoteVersion(noteId, userId);

        // Assert
        verify(noteVersionService).saveNoteVersion(noteId, userId);
        verifyNoMoreInteractions(noteVersionService);
    }

    private DiffRequest createMockDiffRequest() {
        DiffRequest diffRequest = new DiffRequest();
        diffRequest.setNoteId(1L);
        diffRequest.setUserId(42L);
        diffRequest.setDiff("some-diff");
        return diffRequest;
    }

    private Note createMockNote(Long noteId) {
        Note mockNote = new Note();
        mockNote.setId(noteId);
        mockNote.setContent("Sample content");
        return mockNote;
    }
}
