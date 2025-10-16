package com.ChoreTracker.ChoreTracker.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ChoreTracker.ChoreTracker.models.Household;

public interface HouseholdRepository extends MongoRepository<Household, String> {
	boolean existsByInviteCode(String inviteCode);

    Optional<Household> findByInviteCode(String inviteCode);
}
