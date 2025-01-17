package com.cloudComputing.collaborativeNotes.database.repositories;

import com.cloudComputing.collaborativeNotes.database.entities.NoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {

    @Query("SELECT MAX(nv.versionNumber) FROM NoteVersion nv WHERE nv.note.id = :noteId")
    Optional<Integer> findLatestVersionNumberByNoteId(@Param("noteId") Long noteId);
}
