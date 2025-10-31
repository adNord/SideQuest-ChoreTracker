package com.ChoreTracker.ChoreTracker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ChoreTracker.ChoreTracker.dtos.household.CreateHouseholdRequest;
import com.ChoreTracker.ChoreTracker.models.Household;
import com.ChoreTracker.ChoreTracker.models.User;
import com.ChoreTracker.ChoreTracker.repositories.HouseholdRepository;
import com.ChoreTracker.ChoreTracker.repositories.UserRepository;
import com.ChoreTracker.ChoreTracker.service.HouseholdService;
import com.ChoreTracker.ChoreTracker.service.InviteCodeService;

public class HouseholdServiceTest {
    
    @Mock
    private HouseholdRepository householdRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private InviteCodeService inviteCodeService;
    
    private HouseholdService householdService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        householdService = new HouseholdService(householdRepository, userRepository, inviteCodeService);
    }
    
    @Test
    void createHousehold_ShouldCreateHouseholdSuccessfully() {
        var user = new User("testuser", "password");
        user.setHouseholdId(null);
        
        var savedHousehold = new Household("Test hushåll", "user123", new java.util.ArrayList<>(), "gladsolkatt", false);
        var request = new CreateHouseholdRequest("Test hushåll");
        
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(inviteCodeService.generateInviteCode()).thenReturn("gladsolkatt");
        when(householdRepository.save(any(Household.class))).thenReturn(savedHousehold);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        ResponseEntity<Object> response = householdService.createHousehold(request, "user123");
        
        assert response.getStatusCode() == HttpStatus.CREATED;
        assertNotNull(response.getBody());
        
        var resultHousehold = (Household) response.getBody();
        assertEquals("Test hushåll", resultHousehold.getName());
        assertEquals("user123", resultHousehold.getOwnerId());
        assertEquals("gladsolkatt", resultHousehold.getInviteCode());
        
        verify(userRepository).findById("user123");
        verify(inviteCodeService).generateInviteCode();
        verify(householdRepository).save(any(Household.class));
        verify(userRepository).save(any(User.class));
    }
}
