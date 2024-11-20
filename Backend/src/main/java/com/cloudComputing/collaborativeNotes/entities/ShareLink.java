package com.cloudComputing.collaborativeNotes.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "share_link")
public class ShareLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private Note note;

    private String link;
    private String accessLevel;
    private LocalDateTime expirationDate;

    // Getters y Setters
}