package com.ChoreTracker.ChoreTracker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ChoreTracker.ChoreTracker.dtos.RegisterRequest;
import com.ChoreTracker.ChoreTracker.models.User;
import com.ChoreTracker.ChoreTracker.repositories.UserRepository;
import com.ChoreTracker.ChoreTracker.service.JWTService;
import com.ChoreTracker.ChoreTracker.service.UserService;

public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AuthenticationManager authManager;
    
    @Mock
    private JWTService jwtService;
    
    @Mock
    private BCryptPasswordEncoder encoder;
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, authManager, jwtService, encoder);
    }
    
    @Test
    void registerUser_ShouldCreateUser() {
        var request = new RegisterRequest("testuser", "password123", "password123");
        var savedUser = new User("testuser", "encodedPassword");
        
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        when(encoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        ResponseEntity<Object> response = userService.register(request);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        verify(userRepository).findByUsername("testuser");
        verify(encoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
}
