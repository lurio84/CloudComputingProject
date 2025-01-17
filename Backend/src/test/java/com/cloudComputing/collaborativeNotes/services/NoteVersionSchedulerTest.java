package com.cloudComputing.collaborativeNotes.services;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class NoteVersionSchedulerTest {

    @InjectMocks
    private NoteVersionScheduler noteVersionScheduler;

    @Mock
    private NoteVersionService noteVersionService;

    @Mock
    private NoteRepository noteRepository;

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
    void testAutoSaveAllNoteVersions() {
        // Arrange
        Note note1 = new Note();
        note1.setId(1L);

        Note note2 = new Note();
        note2.setId(2L);

        List<Note> mockNotes = Arrays.asList(note1, note2);

        when(noteRepository.findAll()).thenReturn(mockNotes);

        // Act
        noteVersionScheduler.autoSaveAllNoteVersions();

        // Assert
        verify(noteRepository, times(1)).findAll();
        verify(noteVersionService, times(1)).saveNoteVersion(1L, 1L); // Assuming fixed user ID of 1L
        verify(noteVersionService, times(1)).saveNoteVersion(2L, 1L);
    }
}
