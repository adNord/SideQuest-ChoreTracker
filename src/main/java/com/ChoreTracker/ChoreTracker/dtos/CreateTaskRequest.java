package com.ChoreTracker.ChoreTracker.dtos;

import java.time.LocalDateTime;

public record CreateTaskRequest(String title, int score, int frequencyDays, LocalDateTime dueDate) {
    
}
