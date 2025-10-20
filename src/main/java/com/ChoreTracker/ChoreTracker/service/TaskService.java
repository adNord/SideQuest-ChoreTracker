package com.ChoreTracker.ChoreTracker.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.ChoreTracker.ChoreTracker.dtos.CreateTaskRequest;
import com.ChoreTracker.ChoreTracker.models.Household;
import com.ChoreTracker.ChoreTracker.models.Task;
import com.ChoreTracker.ChoreTracker.models.User;
import com.ChoreTracker.ChoreTracker.repositories.HouseholdRepository;
import com.ChoreTracker.ChoreTracker.repositories.UserRepository;

@Service
public class TaskService {

    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;

    public TaskService(HouseholdRepository householdRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /* -- Skapa ny uppgift -- */
    public ResponseEntity<Object> createTask(CreateTaskRequest taskRequest, String userId) {
        //Kolla om användaren finns
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userId + " not found");
        }

        //Kolla om användaren är med i ett hushåll
        String householdId = userOptional.get().getHouseholdId();
        if (householdId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with ID " + userId + " is not in a household");
        }

        //Kolla om hushållet finns
        Optional<Household> householdOptional = householdRepository.findById(householdId);
        if (householdOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Household with ID " + householdId + " not found");
        }

        Task newTask = new Task();
        newTask.setTitle(taskRequest.title());
        newTask.setScore(taskRequest.score());
        newTask.setFrequencyDays(taskRequest.frequencyDays());
        newTask.setDueDate(taskRequest.dueDate());

        // Skicka websocket-meddelande till alla i hushållet om den nya uppgiften FÖRUTOM skaparen
        Household household = householdOptional.get();
        if (household.getMembers() != null) {
            for (Household.MemberScore member : household.getMembers()) {
                String memberId = member.getMemberId();
                if(memberId != null && !memberId.equals(userId)) continue;

                Map<String, Object> content = new HashMap<>();
                content.put("CREATED", newTask);
                messagingTemplate.convertAndSendToUser(memberId, "queue/household/" + householdId + "/tasks", content);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }
    
}
