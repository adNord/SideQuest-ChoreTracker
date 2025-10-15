package com.ChoreTracker.ChoreTracker.service;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.ChoreTracker.ChoreTracker.repositories.HouseholdRepository;

@Service
public class InviteCodeService {
    private final Random random = new Random();
    private final CodeWordProvider codeWordProvider;
    private final HouseholdRepository householdRepository;

    public InviteCodeService(CodeWordProvider codeWordProvider, HouseholdRepository householdRepository) {
        this.codeWordProvider = codeWordProvider;
        this.householdRepository = householdRepository;
    }

    // slumpa fram två unika adjektiv och ett substantiv från listorna och sätt ihop till en sträng
    public String generateInviteCode() {
        List<String> adjectives = codeWordProvider.getAdjectives();
        List<String> nouns = codeWordProvider.getNouns();
        int codegenAttempts = 0;

        final int MAX_ATTEMPTS = 50;
        while (codegenAttempts < MAX_ATTEMPTS) {
            String adjective1 = adjectives.get(random.nextInt(adjectives.size()));
            String adjective2;

            // försök att hitta ett annat adjektiv ett begränsat antal gånger för att undvika oändlig loop
            int attempts = 0;
            do {
                adjective2 = adjectives.get(random.nextInt(adjectives.size()));
                attempts++;
            } while (adjective1.equals(adjective2) && attempts < 10);

            String noun = nouns.get(random.nextInt(nouns.size()));

            String candidate = adjective1 + adjective2 + noun;

            if (!householdRepository.existsByInviteCode(candidate)) {
                return candidate;
            }

            codegenAttempts++;
        }

        throw new IllegalStateException("Unable to generate a unique invite code after " + MAX_ATTEMPTS + " attempts");
    }
}