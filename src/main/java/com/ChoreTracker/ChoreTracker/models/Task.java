package com.ChoreTracker.ChoreTracker.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tasks")
public class Task {
    @Id
    private String id;
    private String householdId;
    private String title;
    private int score;
    private int frequencyDays;
    private LocalDateTime dueDate;

    public Task(String householdId, String title, int score, int frequencyDays, LocalDateTime dueDate) {
        this.householdId = householdId;
        this.title = title;
        this.score = score;
        this.frequencyDays = frequencyDays;
        this.dueDate = dueDate;
    }

    public Task() {}

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getHouseholdId() {
        return householdId;
    }
    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getFrequencyDays() {
        return frequencyDays;
    }
    public void setFrequencyDays(int frequencyDays) {
        this.frequencyDays = frequencyDays;
    }
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    
}
