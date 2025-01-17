package com.cloudComputing.collaborativeNotes.database.repositories;

import com.cloudComputing.collaborativeNotes.database.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    @Query("SELECT n FROM Note n JOIN n.userNotes un WHERE un.user.id = :userId")
    List<Note> findByUserId(@Param("userId") Long userId);
}
