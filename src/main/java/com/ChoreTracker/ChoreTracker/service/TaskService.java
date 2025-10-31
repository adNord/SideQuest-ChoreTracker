package com.ChoreTracker.ChoreTracker.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.ChoreTracker.ChoreTracker.dtos.CreateTaskRequest;
import com.ChoreTracker.ChoreTracker.models.Household;
import com.ChoreTracker.ChoreTracker.models.Task;
import com.ChoreTracker.ChoreTracker.models.User;
import com.ChoreTracker.ChoreTracker.repositories.HouseholdRepository;
import com.ChoreTracker.ChoreTracker.repositories.TaskRepository;
import com.ChoreTracker.ChoreTracker.repositories.UserRepository;

@Service
public class TaskService {

    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    private final SimpMessagingTemplate messagingTemplate;

    public TaskService(HouseholdRepository householdRepository, UserRepository userRepository,
            TaskRepository taskRepository, SimpMessagingTemplate messagingTemplate) {
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /* -- Skapa ny uppgift -- */
    public ResponseEntity<Object> createTask(CreateTaskRequest taskRequest, String userId) {
        // Kolla om användaren finns
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userId + " not found");
        }

        // Kolla om användaren är med i ett hushåll
        String householdId = userOptional.get().getHouseholdId();
        if (householdId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User with ID " + userId + " is not in a household");
        }

        // Kolla om hushållet finns
        Optional<Household> householdOptional = householdRepository.findById(householdId);
        if (householdOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Household with ID " + householdId + " not found");
        }

        Task newTask = new Task();
        newTask.setTitle(taskRequest.title());
        newTask.setScore(taskRequest.score());
        newTask.setFrequencyDays(taskRequest.frequencyDays());
        newTask.setDueDate(taskRequest.dueDate());
        newTask.setHouseholdId(householdId);
        taskRepository.save(newTask);

        // Skicka websocket-meddelande till alla i hushållet om den nya uppgiften
        // Förutom skaparen
        Map<String, Object> content = new HashMap<>();
        content.put("CREATED", newTask);
        notifyHouseholdMembersExcept(householdId, userId, content);

        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    /* -- Slutför uppgift -- */
    public ResponseEntity<Object> completeTask(String userId, String taskId) {
        // kolla så att användaren har rätt att slutföra uppgiften (finns och med i
        // hushåll)
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userId + " not found");
        }

        if (taskOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task with ID " + taskId + " not found");
        }

        Task task = taskOptional.get();

        String userHouseholdId = userOptional.get().getHouseholdId();
        String taskHouseholdId = task.getHouseholdId();

        if (!Objects.equals(userHouseholdId, taskHouseholdId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User with ID " + userId + " cannot complete this task. (Not in the same household)");
        }

        // uppdatera dueDate till nu + frequencyDays
        int frequencyDays = task.getFrequencyDays();

        task.setDueDate(Instant.now().plusSeconds(frequencyDays * 24 * 60 * 60));
        taskRepository.save(task);

        // TODO: premium feature - lägg till i historik

        // uppdatera användarens poäng i hushållet
        Optional<Household> householdOptional = householdRepository.findById(task.getHouseholdId());
        if (!householdOptional.isEmpty()) {
            Household household = householdOptional.get();
            User user = userOptional.get();

            if (household.getMembers() == null) {
                household.setMembers(new ArrayList<>());
            }

            for (Household.MemberScore member : household.getMembers()) {
                if (member.getUser() != null && member.getUser().getId().equals(user.getId())) {
                    member.incrementScore(task.getScore());
                    break;
                }
            }

            householdRepository.save(household);
        }

        // skicka websocket-meddelande till alla utom den som uppdaterade uppgiften i
        // hushållet om den uppdaterade uppgiften (COMPLETED + newDueDate)

        Map<String, Object> content = new HashMap<>();
        content.put("COMPLETED", task);
        notifyHouseholdMembersExcept(task.getHouseholdId(), userId, content);

        // returnera responseentity med uppdaterad uppgift
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    /* -- Hämta alla uppgifter för ett hushåll -- */
    public ResponseEntity<Object> getAllTasksForUserHousehold(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userId + " not found");
        }

        String householdId = userOptional.get().getHouseholdId();
        if (householdId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User with ID " + userId + " is not in a household");
        }
        ArrayList<Task> tasks = taskRepository.findByHouseholdId(householdId);
        if (tasks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No tasks found for household with ID " + householdId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }
    
    /* -- Ta bort uppgift -- */
    public ResponseEntity<Object> deleteTask(String userId, String taskId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userId + " not found");
        }

        String householdId = userOptional.get().getHouseholdId();
        if (householdId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User with ID " + userId + " is not in a household");
        }

        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task with ID " + taskId + " not found");
        }

        Task task = taskOptional.get();
        if (!Objects.equals(task.getHouseholdId(), householdId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User with ID " + userId + " cannot delete this task. (Not in the same household)");
        }
        taskRepository.deleteById(taskId);
        notifyHouseholdMembersExcept(task.getHouseholdId(), userId, Map.of("DELETED", task));
        return ResponseEntity.status(HttpStatus.OK).body("Task with ID " + taskId + " deleted successfully");
    }
    /*-------------------------------------------------------------------- */

    /* hjälpmetod för att skicka websocket till alla utom skaparen */
    private void notifyHouseholdMembersExcept(String householdId, String excludingUserId, Map<String, Object> content) {
        Optional<Household> householdOptional = householdRepository.findById(householdId);
        if (householdOptional.isEmpty()) {
            return;
        }

        Optional<User> excludingUserOptional = userRepository.findById(excludingUserId);
        if (excludingUserOptional.isPresent()) {
            content.put("triggeredBy", excludingUserOptional.get().getUsername());
        }

        Household household = householdOptional.get();
        if (household.getMembers() != null) {
            for (Household.MemberScore member : household.getMembers()) {
                User memberUser = member.getUser();
                // skippa null members och den som ska undantas
                if (memberUser == null || memberUser.getId().equals(excludingUserId))
                    continue;
               
                String memberUsername = memberUser.getUsername();
                System.out.println("Notifying member : " + memberUsername);
                messagingTemplate.convertAndSendToUser(memberUsername, "queue/household/" + householdId + "/tasks", content);
            }
        }
    }



}
