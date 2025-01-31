package com.cloudComputing.collaborativeNotes.controllers;

import com.cloudComputing.collaborativeNotes.database.entities.User;
import com.cloudComputing.collaborativeNotes.database.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testGetUserSuccess() {
        // Arrange
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("Test User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        ResponseEntity<User> response = userController.getUser(userId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(mockUser, response.getBody());
    }

    @Test
    void testGetUserNotFound() {
        // Arrange
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userController.getUser(userId));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testCreateUserSuccess() {
        // Arrange
        String username = "New User";
        String email = "newuser@example.com";
        String password = "password123";

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(username);
        savedUser.setEmail(email);
        savedUser.setPassword(password);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simular el ID generado por la base de datos
            return user;
        });

        // Act
        ResponseEntity<User> response = userController.createUser(username, email, password);

        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());
        assertEquals(email, response.getBody().getEmail());
    }

    @Test
    void testCreateUserInternalError() {
        // Arrange
        String username = "New User";
        String email = "newuser@example.com";
        String password = "password123";

        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<User> response = userController.createUser(username, email, password);

        // Assert
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
