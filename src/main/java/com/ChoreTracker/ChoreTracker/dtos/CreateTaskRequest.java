package com.ChoreTracker.ChoreTracker.dtos;

import java.time.Instant;

public record CreateTaskRequest(String title, int score, int frequencyDays, Instant dueDate) {
    
}
