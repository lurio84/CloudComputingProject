package com.cloudComputing.collaborativeNotes.database.repositories;

import com.cloudComputing.collaborativeNotes.database.entities.NoteChange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteChangeRepository extends JpaRepository<NoteChange, Long> {
}