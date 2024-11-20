package com.cloudComputing.collaborativeNotes.models;

public class NoteEditMessage {
    private Long noteId;
    private Long userId;  // Add this field to identify the user making the edit
    private String content;

    // Getters and Setters
    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
