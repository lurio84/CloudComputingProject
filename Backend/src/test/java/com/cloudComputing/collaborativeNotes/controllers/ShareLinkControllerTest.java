package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.ShareLink;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.ShareLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShareLinkControllerTest {

    @InjectMocks
    private ShareLinkController shareLinkController;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private ShareLinkRepository shareLinkRepository;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testCreateShareLinkSuccess() {
        // Arrange
        Long noteId = 1L;
        String accessLevel = "READ_ONLY";

        Note mockNote = new Note();
        mockNote.setId(noteId);

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(mockNote));

        // Act
        ResponseEntity<ShareLink> response = shareLinkController.createShareLink(noteId, accessLevel);

        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(mockNote, response.getBody().getNote());
        assertEquals(accessLevel, response.getBody().getAccessLevel());
        verify(shareLinkRepository).save(any(ShareLink.class));
    }

    @Test
    void testCreateShareLinkNoteNotFound() {
        // Arrange
        Long noteId = 1L;
        String accessLevel = "READ_ONLY";

        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ShareLink> response = shareLinkController.createShareLink(noteId, accessLevel);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(shareLinkRepository, never()).save(any(ShareLink.class));
    }

    @Test
    void testGetNoteByLinkSuccess() {
        // Arrange
        String link = "valid-share-link";
        Note mockNote = new Note();
        mockNote.setId(1L);

        ShareLink mockShareLink = new ShareLink();
        mockShareLink.setLink(link);
        mockShareLink.setNote(mockNote);
        mockShareLink.setExpirationDate(LocalDateTime.now().plusDays(1));

        when(shareLinkRepository.findByLink(link)).thenReturn(Optional.of(mockShareLink));

        // Act
        ResponseEntity<Note> response = shareLinkController.getNoteByLink(link);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(mockNote, response.getBody());
    }

    @Test
    void testGetNoteByLinkNotFound() {
        // Arrange
        String link = "invalid-share-link";

        when(shareLinkRepository.findByLink(link)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Note> response = shareLinkController.getNoteByLink(link);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testGetNoteByLinkExpired() {
        // Arrange
        String link = "expired-share-link";
        Note mockNote = new Note();
        mockNote.setId(1L);

        ShareLink mockShareLink = new ShareLink();
        mockShareLink.setLink(link);
        mockShareLink.setNote(mockNote);
        mockShareLink.setExpirationDate(LocalDateTime.now().minusDays(1));

        when(shareLinkRepository.findByLink(link)).thenReturn(Optional.of(mockShareLink));

        // Act
        ResponseEntity<Note> response = shareLinkController.getNoteByLink(link);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
