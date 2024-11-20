package com.cloudComputing.collaborativeNotes.database.repositories;

import com.cloudComputing.collaborativeNotes.database.entities.NoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {
}
