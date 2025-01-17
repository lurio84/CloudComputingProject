package com.cloudComputing.collaborativeNotes.database.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_note")
public class UserNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-userNote")
    private User user;

    @ManyToOne
    @JoinColumn(name = "note_id")
    @JsonBackReference(value = "note-userNote")
    private Note note;

    private String accessLevel;
    private LocalDateTime addedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
