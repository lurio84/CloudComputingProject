package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.ShareLink;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.ShareLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/share")
public class ShareLinkController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ShareLinkRepository shareLinkRepository;

    // Endpoint to create a new share link
    @PostMapping("/create/{noteId}")
    public ResponseEntity<ShareLink> createShareLink(@PathVariable Long noteId, @RequestParam String accessLevel) {
        Optional<Note> optionalNote = noteRepository.findById(noteId);
        if (optionalNote.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Note note = optionalNote.get();

        ShareLink shareLink = new ShareLink();
        shareLink.setNote(note);
        shareLink.setLink(UUID.randomUUID().toString());
        shareLink.setAccessLevel(accessLevel);
        shareLink.setExpirationDate(LocalDateTime.now().plusDays(7)); // Default expiration of 7 days

        shareLinkRepository.save(shareLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(shareLink);
    }

    // Endpoint to access a note via share link
    @GetMapping("/{link}")
    public ResponseEntity<Note> getNoteByLink(@PathVariable String link) {
        Optional<ShareLink> optionalShareLink = shareLinkRepository.findByLink(link);
        if (optionalShareLink.isEmpty() || optionalShareLink.get().getExpirationDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ShareLink shareLink = optionalShareLink.get();
        Note note = shareLink.getNote();

        return ResponseEntity.ok(note);
    }
}
