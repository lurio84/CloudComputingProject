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
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

        String cacheKey = DIFF_CACHE_KEY_PREFIX + note.getId();
        String diffListKey = DIFF_LIST_KEY_PREFIX + note.getId();

        redisTemplate.opsForList().rightPush(cacheKey, diffRequest.getDiff());
        redisTemplate.opsForList().rightPush(diffListKey, diffRequest);
        redisTemplate.expire(cacheKey, 30, TimeUnit.MINUTES);
        redisTemplate.expire(diffListKey, 30, TimeUnit.MINUTES);

        messagingTemplate.convertAndSend("/topic/notes/" + note.getId(), diffRequest);
        logger.info("Diff cached and sent to clients for noteId: {}", note.getId());
    }

    public Note getNoteWithCachedDiffs(Long noteId) {
        // Obtener la nota desde la base de datos
        Note note = getNoteById(noteId);
        String content = note.getContent();

        logger.info("Contenido base de la nota con ID {}: {}", noteId, content);

        if (content == null || content.isEmpty()) {
            logger.warn("El contenido base de la nota está vacío o es nulo.");
            return note; // Retornar la nota tal cual si está vacía
        }

        // Obtener los diffs acumulados de Redis
        String cacheKey = DIFF_CACHE_KEY_PREFIX + noteId;
        List<Object> cachedDiffs = redisTemplate.opsForList().range(cacheKey, 0, -1);

        if (cachedDiffs == null || cachedDiffs.isEmpty()) {
            logger.info("No se encontraron diffs en caché para la nota con ID {}", noteId);
            return note; // Si no hay diffs, retornar la nota original
        }

        // Aplicar los diffs al contenido base
        for (Object diff : cachedDiffs) {
            content = applyDiffToContent((String) diff, content);
            logger.info("Contenido actualizado tras aplicar diff: {}", content);
        }

        // Crear una copia de la nota con el contenido actualizado
        Note updatedNote = new Note();
        updatedNote.setId(note.getId());
        updatedNote.setTitle(note.getTitle());
        updatedNote.setContent(content); // Aquí ponemos el contenido con los diffs aplicados
        updatedNote.setCreatedAt(note.getCreatedAt());
        updatedNote.setUpdatedAt(LocalDateTime.now());

        return updatedNote; // Devolver la nota con el contenido actualizado
    }


    @Scheduled(fixedRate = 20000) // Every 20 seconds
    public void processAndSaveCachedDiffs() {
        // ✅ Usamos SCAN en lugar de KEYS para evitar restricciones en AWS ElastiCache
        Set<String> keys = scanKeys(DIFF_CACHE_KEY_PREFIX + "*");

        if (keys.isEmpty()) {
            return;
        }

        for (String cacheKey : keys) {
            Long noteId = extractNoteIdFromCacheKey(cacheKey);
            String diffListKey = DIFF_LIST_KEY_PREFIX + noteId;

            LinkedList<Object> cachedDiffs = new LinkedList<>(Objects.requireNonNull(redisTemplate.opsForList().range(cacheKey, 0, -1)));
            LinkedList<Object> cachedDiffRequests = new LinkedList<>(Objects.requireNonNull(redisTemplate.opsForList().range(diffListKey, 0, -1)));

            if (cachedDiffs.isEmpty() || cachedDiffRequests.isEmpty()) {
                continue;
            }

            Note note = getNoteById(noteId);
            String updatedContent = note.getContent();

            StringBuilder consolidatedDiff = new StringBuilder();
            for (Object cachedDiff : cachedDiffs) {
                updatedContent = applyDiffToContent((String) cachedDiff, updatedContent);
                consolidatedDiff.append(cachedDiff).append("\n");
            }

            NoteChange.ChangeType aggregatedChangeType = determineAggregatedChangeType(consolidatedDiff.toString());

            note.setContent(updatedContent);
            note.setUpdatedAt(LocalDateTime.now());
            noteRepository.save(note);

            DiffRequest firstDiffRequest = (DiffRequest) cachedDiffRequests.getFirst();
            User user = getUserById(firstDiffRequest.getUserId());
            saveNoteChange(note, user, consolidatedDiff.toString(), aggregatedChangeType);

            redisTemplate.delete(cacheKey);
            redisTemplate.delete(diffListKey);
            logger.info("Saved and cleared cached diffs for noteId: {}", noteId);
        }
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        try (var cursor = redisTemplate.getConnectionFactory().getConnection().scan(options)) {
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
        } catch (Exception e) {
            logger.error("Error scanning Redis keys: {}", e.getMessage());
        }
        return keys;
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
