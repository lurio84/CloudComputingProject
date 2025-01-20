package com.cloudComputing.collaborativeNotes.services;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.entities.UserNote;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteChangeRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserNoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import com.cloudComputing.collaborativeNotes.exceptions.NoteNotFoundException;
import com.cloudComputing.collaborativeNotes.exceptions.UserNotFoundException;
import com.cloudComputing.collaborativeNotes.models.DiffRequest;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    private UserNoteRepository userNoteRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final DiffMatchPatch dmp = new DiffMatchPatch();

    private static final String DIFF_CACHE_KEY_PREFIX = "diff_cache_";
    private static final String DIFF_LIST_KEY_PREFIX = "diff_list_";

    public void applyDiffAndNotifyClients(DiffRequest diffRequest) {
        validateDiffRequest(diffRequest);

        Note note = getNoteById(diffRequest.getNoteId());
        User user = getUserById(diffRequest.getUserId());

        // Accumulate the diff in Redis for both content update and persistence
        String cacheKey = DIFF_CACHE_KEY_PREFIX + note.getId();
        String diffListKey = DIFF_LIST_KEY_PREFIX + note.getId();

        redisTemplate.opsForList().rightPush(cacheKey, diffRequest.getDiff());
        redisTemplate.opsForList().rightPush(diffListKey, diffRequest);
        redisTemplate.expire(cacheKey, 30, TimeUnit.MINUTES); // TTL of 30 minutes for diffs
        redisTemplate.expire(diffListKey, 30, TimeUnit.MINUTES); // TTL of 30 minutes for diffs

        // Notify other clients
        messagingTemplate.convertAndSend("/topic/notes/" + note.getId(), diffRequest);
        logger.info("Diff cached and sent to clients for noteId: {}", note.getId());
    }

    @Scheduled(fixedRate = 20000) // Every 20 seconds
    public void processAndSaveCachedDiffs() {
        // Retrieve all cache keys corresponding to notes
        Set<String> keys = redisTemplate.keys(DIFF_CACHE_KEY_PREFIX + "*");
        if (keys.isEmpty()) {
            return;
        }

        for (String cacheKey : keys) {
            Long noteId = extractNoteIdFromCacheKey(cacheKey);
            String diffListKey = DIFF_LIST_KEY_PREFIX + noteId;

            // Process accumulated diffs
            LinkedList<Object> cachedDiffs = new LinkedList<>(Objects.requireNonNull(redisTemplate.opsForList().range(cacheKey, 0, -1)));
            LinkedList<Object> cachedDiffRequests = new LinkedList<>(Objects.requireNonNull(redisTemplate.opsForList().range(diffListKey, 0, -1)));

            if (cachedDiffs.isEmpty() || cachedDiffRequests.isEmpty()) {
                continue;
            }

            Note note = getNoteById(noteId);
            String updatedContent = note.getContent();

            // Consolidate all diffs into a single diff string
            StringBuilder consolidatedDiff = new StringBuilder();
            for (Object cachedDiff : cachedDiffs) {
                updatedContent = applyDiffToContent((String) cachedDiff, updatedContent);
                consolidatedDiff.append(cachedDiff).append("\n");
            }

            // Determine the aggregated change type
            NoteChange.ChangeType aggregatedChangeType = determineAggregatedChangeType(consolidatedDiff.toString());

            // Save the note content to the database
            note.setContent(updatedContent);
            note.setUpdatedAt(LocalDateTime.now());
            noteRepository.save(note);

            // Save the consolidated diff as a single record in the database
            DiffRequest firstDiffRequest = (DiffRequest) cachedDiffRequests.getFirst();
            User user = getUserById(firstDiffRequest.getUserId());
            saveNoteChange(note, user, consolidatedDiff.toString(), aggregatedChangeType);

            // Clear the cache after saving
            redisTemplate.delete(cacheKey);
            redisTemplate.delete(diffListKey);
            logger.info("Saved and cleared cached diffs for noteId: {}", noteId);
        }
    }

    private Long extractNoteIdFromCacheKey(String cacheKey) {
        return Long.valueOf(cacheKey.replace(DIFF_CACHE_KEY_PREFIX, ""));
    }

    public Note getNoteById(Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    public void assignUserToNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create the relationship between the note and the user
        UserNote userNote = new UserNote();
        userNote.setNote(note);
        userNote.setUser(user);
        userNote.setAccessLevel(UserNote.AccessLevel.EDITOR); // Set the default access level or customize as needed
        userNote.setAddedAt(LocalDateTime.now());
        userNoteRepository.save(userNote);
    }

    public Note createNote(Note newNote, Long userId) {
        // Fetch the user from the database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set the creation and update timestamps
        newNote.setCreatedAt(LocalDateTime.now());
        newNote.setUpdatedAt(LocalDateTime.now());

        // Create the UserNote relationship
        UserNote userNote = new UserNote();
        userNote.setUser(user);
        userNote.setNote(newNote);
        userNote.setAccessLevel(UserNote.AccessLevel.EDITOR); // Assign a default access level
        userNote.setAddedAt(LocalDateTime.now());

        // Add the relationship to the note's user notes collection
        newNote.getUserNotes().add(userNote);

        // Save the note (cascading also saves UserNote)
        return noteRepository.save(newNote);
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

    private NoteChange.ChangeType determineAggregatedChangeType(String consolidatedDiff) {
        boolean hasInsert = false;
        boolean hasDelete = false;

        LinkedList<DiffMatchPatch.Patch> patches = (LinkedList<DiffMatchPatch.Patch>) dmp.patchFromText(consolidatedDiff);
        for (DiffMatchPatch.Patch patch : patches) {
            for (DiffMatchPatch.Diff diff : patch.diffs) {
                if (diff.operation == DiffMatchPatch.Operation.INSERT) {
                    hasInsert = true;
                } else if (diff.operation == DiffMatchPatch.Operation.DELETE) {
                    hasDelete = true;
                }
            }
        }

        if (hasInsert && hasDelete) {
            return NoteChange.ChangeType.EDITED;
        } else if (hasInsert) {
            return NoteChange.ChangeType.ADDED;
        } else if (hasDelete) {
            return NoteChange.ChangeType.DELETED;
        } else {
            return NoteChange.ChangeType.UNCHANGED;
        }
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
