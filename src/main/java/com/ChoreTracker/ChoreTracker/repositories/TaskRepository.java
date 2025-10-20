package com.ChoreTracker.ChoreTracker.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ChoreTracker.ChoreTracker.models.Task;

public interface TaskRepository extends MongoRepository<Task, String> {
    
}
