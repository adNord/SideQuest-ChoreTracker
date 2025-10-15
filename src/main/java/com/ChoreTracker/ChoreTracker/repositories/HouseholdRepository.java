package com.ChoreTracker.ChoreTracker.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ChoreTracker.ChoreTracker.models.Household;

public interface HouseholdRepository extends MongoRepository<Household, String> {
    
}
