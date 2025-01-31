package com.cloudComputing.collaborativeNotes.database.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_note")
public class UserNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // To maintain compatibility with the existing database schema
    private Long userNoteId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-userNote") // Prevent circular serialization of user references
    private User user;

    @ManyToOne
    @JoinColumn(name = "note_id")
    @JsonBackReference(value = "note-userNote") // Prevent circular serialization of note references
    private Note note;

    @Enumerated(EnumType.STRING)
    private AccessLevel accessLevel;

    private LocalDateTime addedAt;

    // Getters and Setters
    public Long getUserNoteId() {
        return userNoteId;
    }

    public void setUserNoteId(Long userNoteId) {
        this.userNoteId = userNoteId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public enum AccessLevel {
        EDITOR,
        VIEWER
    }
}
