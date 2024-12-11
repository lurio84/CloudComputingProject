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
        User newUser = new User();
        newUser.setUsername("New User");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("New User");

        when(userRepository.save(newUser)).thenReturn(savedUser);

        // Act
        ResponseEntity<User> response = userController.createUser(newUser);

        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(savedUser, response.getBody());
    }

    @Test
    void testCreateUserInternalError() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("New User");

        when(userRepository.save(newUser)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<User> response = userController.createUser(newUser);

        // Assert
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}