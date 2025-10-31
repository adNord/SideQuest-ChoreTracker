package com.ChoreTracker.ChoreTracker.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ChoreTracker.ChoreTracker.dtos.JwtResponse;
import com.ChoreTracker.ChoreTracker.dtos.LoginRequest;
import com.ChoreTracker.ChoreTracker.dtos.RegisterRequest;
import com.ChoreTracker.ChoreTracker.models.User;
import com.ChoreTracker.ChoreTracker.repositories.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository userRepository, AuthenticationManager authManager, JWTService jwtService, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }

    public ResponseEntity<Object> register(RegisterRequest registerRequest) {
        Map<String, String> response = new HashMap<>();

        if(!registerRequest.passwordsMatch()) {
            response.put("message", "Passwords do not match");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User userInDb = userRepository.findByUsername(registerRequest.username());

        if (userInDb != null) {
            response.put("error", "A user with that username already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        String encodedPassword = encoder.encode(registerRequest.password());

        User newUser = new User(registerRequest.username(), encodedPassword);
        
        userRepository.save(newUser);
        response.put("message", "User registered successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public ResponseEntity<Object> login(LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = 
                authManager.authenticate(new UsernamePasswordAuthenticationToken(
            loginRequest.username(), loginRequest.password()));

        if (authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(loginRequest.username());
            String jwt = jwtService.generateToken(user);
            return ResponseEntity.ok(new JwtResponse(jwt));
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public ResponseEntity<Object> getCurrentUser(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userId + " not found");
        }
        Map<String, Object> user = new HashMap<>();
        user.put("id", userOptional.get().getId());
        user.put("username", userOptional.get().getUsername());
        user.put("householdId", userOptional.get().getHouseholdId());
        user.put("isPremium", userOptional.get().isPremium());
        return ResponseEntity.ok(user);
    }
}