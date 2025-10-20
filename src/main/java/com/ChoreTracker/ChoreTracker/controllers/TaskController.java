package com.ChoreTracker.ChoreTracker.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ChoreTracker.ChoreTracker.dtos.CreateTaskRequest;
import com.ChoreTracker.ChoreTracker.models.UserPrincipal;
import com.ChoreTracker.ChoreTracker.service.TaskService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/task")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Object> postNewTask(CreateTaskRequest taskRequest, Authentication authentication) {
        String userId = getUserId(authentication);
        
        return taskService.createTask(taskRequest, userId);
    }

    private String getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) return up.getId();
        return "";
    }
    
}
