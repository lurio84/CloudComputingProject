package com.cloudComputing.collaborativeNotes.database.repositories;

import com.cloudComputing.collaborativeNotes.database.entities.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByLink(String link);
}