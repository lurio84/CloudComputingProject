package com.cloudComputing.collaborativeNotes.services;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteChangeRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import com.cloudComputing.collaborativeNotes.exceptions.NoteNotFoundException;
import com.cloudComputing.collaborativeNotes.exceptions.UserNotFoundException;
import com.cloudComputing.collaborativeNotes.models.DiffRequest;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;

@Service
public class NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteChangeRepository noteChangeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final DiffMatchPatch dmp = new DiffMatchPatch();

    public Note applyDiffToNote(DiffRequest diffRequest) {
        validateDiffRequest(diffRequest);

        Note note = getNoteById(diffRequest.getNoteId());
        User user = getUserById(diffRequest.getUserId());

        String originalContent = note.getContent();
        String updatedContent = applyDiffToContent(diffRequest.getDiff(), originalContent);

        note.setContent(updatedContent);
        noteRepository.save(note);

        NoteChange.ChangeType changeType = determineChangeType(diffRequest.getDiff());
        saveNoteChange(note, user, diffRequest.getDiff(), changeType);

        // Notify other clients
        messagingTemplate.convertAndSend("/topic/notes/" + note.getId(), diffRequest);
        logger.info("Diff applied and sent to clients for noteId: {}", note.getId());

        return note;
    }

    public Note getNoteById(Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private void validateDiffRequest(DiffRequest diffRequest) {
        if (diffRequest == null || diffRequest.getDiff() == null || diffRequest.getDiff().isEmpty()) {
            throw new IllegalArgumentException("Invalid DiffRequest");
        }
    }

    private String applyDiffToContent(String diff, String originalContent) {
        LinkedList<DiffMatchPatch.Patch> patches = (LinkedList<DiffMatchPatch.Patch>) dmp.patchFromText(diff);
        Object[] result = dmp.patchApply(patches, originalContent);
        return (String) result[0];
    }

    private NoteChange.ChangeType determineChangeType(String diff) {
        LinkedList<DiffMatchPatch.Patch> patches = (LinkedList<DiffMatchPatch.Patch>) dmp.patchFromText(diff);
        for (DiffMatchPatch.Patch patch : patches) {
            for (DiffMatchPatch.Diff diffItem : patch.diffs) {
                if (diffItem.operation == DiffMatchPatch.Operation.INSERT) {
                    return NoteChange.ChangeType.ADDED;
                } else if (diffItem.operation == DiffMatchPatch.Operation.DELETE) {
                    return NoteChange.ChangeType.DELETED;
                }
            }
        }
        return NoteChange.ChangeType.EDITED;
    }

    private void saveNoteChange(Note note, User user, String diff, NoteChange.ChangeType changeType) {
        NoteChange noteChange = new NoteChange();
        noteChange.setNote(note);
        noteChange.setUser(user);
        noteChange.setChangeType(changeType);
        noteChange.setDiff(diff);
        noteChange.setTimestamp(LocalDateTime.now());
        noteChangeRepository.save(noteChange);
        logger.info("Note change saved for noteId: {}", note.getId());
    }
}
