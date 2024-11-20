package com.cloudComputing.collaborativeNotes.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "note")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "note")
    private List<NoteChange> changes;

    @OneToMany(mappedBy = "note")
    private List<NoteVersion> versions;

    @OneToMany(mappedBy = "note")
    private List<ShareLink> shareLinks;

    // Getters y Setters
}