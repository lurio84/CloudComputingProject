package com.cloudComputing.collaborativeNotes.services;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteChangeRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import com.cloudComputing.collaborativeNotes.exceptions.NoteNotFoundException;
import com.cloudComputing.collaborativeNotes.models.DiffRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NoteServiceTest {

    @InjectMocks
    private NoteService noteService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteChangeRepository noteChangeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

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
    void testApplyDiffToNoteSuccess() {
        // Arrange
        DiffRequest diffRequest = createMockDiffRequest();
        Note mockNote = createMockNote(1L, "Hello Worl");
        User mockUser = createMockUser();

        when(noteRepository.findById(diffRequest.getNoteId())).thenReturn(Optional.of(mockNote));
        when(userRepository.findById(diffRequest.getUserId())).thenReturn(Optional.of(mockUser));

        // Act
        Note updatedNote = noteService.applyDiffToNote(diffRequest);

        // Assert
        assertEquals("Hello World", updatedNote.getContent(), "Content should be updated correctly");
        verify(noteRepository).save(mockNote);
        verify(noteChangeRepository).save(any(NoteChange.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/notes/" + mockNote.getId()), eq(diffRequest));
    }
***/
    @Test
    void testGetNoteByIdSuccess() {
        // Arrange
        Long noteId = 1L;
        Note mockNote = createMockNote(noteId, "Sample content");

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(mockNote));

        // Act
        Note note = noteService.getNoteById(noteId);

        // Assert
        assertNotNull(note, "Note should not be null");
        assertEquals(noteId, note.getId(), "Note ID should match");
    }

    @Test
    void testGetNoteByIdNotFound() {
        // Arrange
        Long noteId = 1L;

        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoteNotFoundException.class, () -> noteService.getNoteById(noteId), "Exception should be thrown if note is not found");
    }

    private DiffRequest createMockDiffRequest() {
        DiffRequest diffRequest = new DiffRequest();
        diffRequest.setNoteId(1L);
        diffRequest.setUserId(42L);
        diffRequest.setDiff("@@ -1,12 +1,13 @@\n Hello Worl\n+d\n");
        return diffRequest;
    }

    private Note createMockNote(Long noteId, String content) {
        Note mockNote = new Note();
        mockNote.setId(noteId);
        mockNote.setContent(content);
        return mockNote;
    }

    private User createMockUser() {
        User mockUser = new User();
        mockUser.setId(42L);
        return mockUser;
    }
}
