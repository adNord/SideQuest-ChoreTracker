package com.ChoreTracker.ChoreTracker.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ChoreTracker.ChoreTracker.models.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
    
}
