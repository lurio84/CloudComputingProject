package com.cloudComputing.collaborativeNotes.exceptions;

public class NoteNotFoundException extends RuntimeException {
    public NoteNotFoundException(String message) {
        super(message);
    }
}
