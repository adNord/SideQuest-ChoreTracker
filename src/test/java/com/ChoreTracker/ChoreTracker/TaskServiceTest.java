package com.ChoreTracker.ChoreTracker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.ChoreTracker.ChoreTracker.models.Household;
import com.ChoreTracker.ChoreTracker.models.Task;
import com.ChoreTracker.ChoreTracker.models.User;
import com.ChoreTracker.ChoreTracker.repositories.HouseholdRepository;
import com.ChoreTracker.ChoreTracker.repositories.TaskRepository;
import com.ChoreTracker.ChoreTracker.repositories.UserRepository;
import com.ChoreTracker.ChoreTracker.service.TaskService;

public class TaskServiceTest {
    
    @Mock
    private HouseholdRepository householdRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    private TaskService taskService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskService = new TaskService(householdRepository, userRepository, taskRepository, messagingTemplate);
    }
    
    @Test
    void completeTask_ShouldUpdateTaskDate() {       
        var task = new Task();
        task.setTitle("Diska");
        task.setScore(1);
        task.setFrequencyDays(7);
        task.setDueDate(Instant.now());
        task.setHouseholdId("household123");
        
        var household = new Household("Test hushåll", "user123", new ArrayList<>(), "gladsolkatt", false);
        
        // Skapar användare med id
        var userWithId = new User("testuser", "password");
        userWithId.setHouseholdId("household123");
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userWithId, "user123");
        } catch (Exception e) {
            fail("Could not set user ID: " + e.getMessage());
        }
        
        household.getMembers().add(new Household.MemberScore(userWithId, 0));
        
        when(userRepository.findById("user123")).thenReturn(Optional.of(userWithId));
        when(taskRepository.findById("task123")).thenReturn(Optional.of(task));
        when(householdRepository.findById("household123")).thenReturn(Optional.of(household));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(householdRepository.save(any(Household.class))).thenReturn(household);
        
        Instant beforeComplete = Instant.now();
        ResponseEntity<Object> response = taskService.completeTask("user123", "task123");
        Instant afterComplete = Instant.now();
        
        assert response.getStatusCode() == HttpStatus.OK;
        
        var resultTask = (Task) response.getBody();
        assertNotNull(resultTask.getDueDate());
        
        // använd en tolerans för tidsskiillnad
        Instant expectedDate = beforeComplete.plusSeconds(7 * 24 * 60 * 60);
        Instant maxExpectedDate = afterComplete.plusSeconds(7 * 24 * 60 * 60);
        
        assertTrue(resultTask.getDueDate().isAfter(expectedDate.minusSeconds(1)) && 
                   resultTask.getDueDate().isBefore(maxExpectedDate.plusSeconds(1)));
        
        verify(taskRepository).save(any(Task.class));
    }
}
