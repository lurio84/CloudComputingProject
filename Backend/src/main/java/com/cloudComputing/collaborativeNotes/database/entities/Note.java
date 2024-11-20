package com.cloudComputing.collaborativeNotes.database.entities;

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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<NoteChange> getChanges() {
        return changes;
    }

    public void setChanges(List<NoteChange> changes) {
        this.changes = changes;
    }

    public List<NoteVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<NoteVersion> versions) {
        this.versions = versions;
    }

    public List<ShareLink> getShareLinks() {
        return shareLinks;
    }

    public void setShareLinks(List<ShareLink> shareLinks) {
        this.shareLinks = shareLinks;
    }
}