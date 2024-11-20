package com.cloudComputing.collaborativeNotes.database.seeders;

import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import org.springframework.stereotype.Component;

// Seeder class for User entity
@Component
public class UserSeeder {

    private final UserRepository userRepository;

    public UserSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Method to seed the User table
    public void seed() {
        if (userRepository.count() == 0) { // Check if User table is empty
            User user = new User();
            user.setUsername("john_doe");
            user.setEmail("john@example.com");
            user.setPassword("password123");
            userRepository.save(user); // Save the user to the database
        }
    }
}
