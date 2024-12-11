package com.cloudComputing.collaborativeNotes.services;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteVersionRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NoteVersionServiceTest {

    @InjectMocks
    private NoteVersionService noteVersionService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteVersionRepository noteVersionRepository;

    @Mock
    private UserRepository userRepository;

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
    void testSaveNoteVersionSuccess() {
        // Arrange
        Long noteId = 1L;
        Long userId = 2L;

        Note mockNote = new Note();
        mockNote.setId(noteId);
        mockNote.setContent("Sample content");

        User mockUser = new User();
        mockUser.setId(userId);

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(mockNote));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(noteVersionRepository.findLatestVersionNumberByNoteId(noteId)).thenReturn(Optional.of(3));

        // Act
        noteVersionService.saveNoteVersion(noteId, userId);

        // Assert
        verify(noteVersionRepository).save(argThat(noteVersion ->
                noteVersion.getVersionNumber() == 4 &&
                        noteVersion.getContent().equals("Sample content") &&
                        noteVersion.getNote().equals(mockNote) &&
                        noteVersion.getUser().equals(mockUser)
        ));
    }

    @Test
    void testSaveNoteVersionNoteNotFound() {
        // Arrange
        Long noteId = 1L;
        Long userId = 2L;

        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> noteVersionService.saveNoteVersion(noteId, userId));
        assertEquals("Note with ID 1 or User with ID 2 not found", exception.getMessage());
    }

    @Test
    void testSaveNoteVersionUserNotFound() {
        // Arrange
        Long noteId = 1L;
        Long userId = 2L;

        Note mockNote = new Note();
        mockNote.setId(noteId);
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(mockNote));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> noteVersionService.saveNoteVersion(noteId, userId));
        assertEquals("Note with ID 1 or User with ID 2 not found", exception.getMessage());
    }
}