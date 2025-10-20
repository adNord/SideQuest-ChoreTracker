package com.ChoreTracker.ChoreTracker.repositories;

import java.util.ArrayList;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ChoreTracker.ChoreTracker.models.Task;

public interface TaskRepository extends MongoRepository<Task, String> {

    ArrayList<Task> findByHouseholdId(String householdId);
    
}
