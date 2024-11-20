package com.cloudComputing.collaborativeNotes.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "note_version")
public class NoteVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private Note note;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer versionNumber;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime createdAt;

    // Getters y Setters
}