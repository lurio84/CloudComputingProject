package com.cloudComputing.collaborativeNotes.database.seeders;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import com.cloudComputing.collaborativeNotes.database.entities.ShareLink;
import com.cloudComputing.collaborativeNotes.database.repositories.NoteRepository;
import com.cloudComputing.collaborativeNotes.database.repositories.ShareLinkRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Seeder class for ShareLink entity
@Component
public class ShareLinkSeeder {

    private final ShareLinkRepository shareLinkRepository;
    private final NoteRepository noteRepository;

    public ShareLinkSeeder(ShareLinkRepository shareLinkRepository, NoteRepository noteRepository) {
        this.shareLinkRepository = shareLinkRepository;
        this.noteRepository = noteRepository;
    }

    // Method to seed the ShareLink table
    public void seed() {
        if (shareLinkRepository.count() == 0) { // Check if ShareLink table is empty
            Note note = noteRepository.findById(1L).orElseThrow(() -> new RuntimeException("Note not found"));

            ShareLink shareLink = new ShareLink();
            shareLink.setNote(note);
            shareLink.setLink("unique-link-123");
            shareLink.setAccessLevel("viewer");
            shareLink.setExpirationDate(LocalDateTime.now().plusDays(30));
            shareLinkRepository.save(shareLink); // Save the share link to the database
        }
    }
}
