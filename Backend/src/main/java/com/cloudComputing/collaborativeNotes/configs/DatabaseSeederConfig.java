package com.cloudComputing.collaborativeNotes.configs;

import com.cloudComputing.collaborativeNotes.database.seeders.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Main configuration class to run all seeders
@Configuration
public class DatabaseSeederConfig {

    @Bean
    CommandLineRunner runAllSeeders(UserSeeder userSeeder, NoteSeeder noteSeeder, UserNoteSeeder userNoteSeeder, NoteChangeSeeder noteChangeSeeder, ShareLinkSeeder shareLinkSeeder, NoteVersionSeeder noteVersionSeeder) {
        return args -> {
            userSeeder.seed();
            noteSeeder.seed();
            userNoteSeeder.seed();
            noteChangeSeeder.seed();
            shareLinkSeeder.seed();
            noteVersionSeeder.seed();
        };
    }
}