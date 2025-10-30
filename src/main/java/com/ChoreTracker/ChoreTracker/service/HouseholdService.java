package com.ChoreTracker.ChoreTracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ChoreTracker.ChoreTracker.dtos.household.CreateHouseholdRequest;
import com.ChoreTracker.ChoreTracker.models.Household;
import com.ChoreTracker.ChoreTracker.models.User;
import com.ChoreTracker.ChoreTracker.repositories.HouseholdRepository;
import com.ChoreTracker.ChoreTracker.repositories.UserRepository;

@Service
public class HouseholdService {
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final InviteCodeService inviteCodeService;

    public HouseholdService(HouseholdRepository householdRepository, UserRepository userRepository, InviteCodeService inviteCodeService) {
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.inviteCodeService = inviteCodeService;
    }


    /* -- Skapa nytt hushåll -- */
    public ResponseEntity<Object> createHousehold(CreateHouseholdRequest request, String userID) {
        //Kolla om användaren är valid och inte med i ett hushåll redan
        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userID + " not found");
        } else if (userOptional.get().getHouseholdId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with ID " + userID + " already in a household");
        }

        //Skapa nytt hushåll, sätt skaparen som ägare
        Household newHousehold = new Household();
        newHousehold.setName(request.name());
        newHousehold.setOwnerId(userID);

        //Lägg till skaparen som medlem med startpoäng 0
        Household.MemberScore creator = new Household.MemberScore(userID, 0);
        List<Household.MemberScore> members = new java.util.ArrayList<>();
        members.add(creator);
        newHousehold.setMembers(members);

        //Generera unik invite-kod
        String inviteCode = inviteCodeService.generateInviteCode();
        newHousehold.setInviteCode(inviteCode);
        newHousehold.setPremium(false); //Standard är icke-premium

        Household saved = householdRepository.save(newHousehold);

        //Uppdatera användarens householdId och spara
        User user = userOptional.get();
        user.setHouseholdId(saved.getId());
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /* -- Gå med i hushåll -- */
    public ResponseEntity<Object> joinHousehold(String inviteCode, String userID) {
        //Kolla om användaren är valid och inte med i ett hushåll redan
        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userID + " not found");
        } else if (userOptional.get().getHouseholdId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with ID " + userID + " already in a household");
        }

        //Hitta hushållet med inbjudningskoden
        Optional<Household> householdOptional = householdRepository.findByInviteCode(inviteCode);
        if (householdOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No household found with invite code " + inviteCode);
        }
        Household household = householdOptional.get();
        //Lägg till användaren som medlem med startpoäng 0
        List<Household.MemberScore> members = household.getMembers();
        members.add(new Household.MemberScore(userID, 0));
        household.setMembers(members);
        householdRepository.save(household);

        User user = userOptional.get();
        user.setHouseholdId(household.getId());
        userRepository.save(user);

        Household joinedHousehold = householdRepository.findById(household.getId()).orElse(null);
        return ResponseEntity.status(HttpStatus.OK).body(joinedHousehold);
    }

    /* -- Lämna hushåll -- */
    public ResponseEntity<Object> leaveHousehold(String householdId, String userID) {
        // Kolla om användaren är valid och med i ett hushåll
        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userID + " not found");
        } else if (!userOptional.get().getHouseholdId().equals(householdId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with ID " + userID + " is not in household " + householdId);
        }

        // Ta bort användaren från hushållet
        Household household = householdRepository.findById(householdId).orElse(null);
        if (household != null) {
            household.getMembers().removeIf(member -> member.getMemberId().equals(userID));
            householdRepository.save(household);
        }

        // Uppdatera användarens householdId och spara
        User user = userOptional.get();
        user.setHouseholdId(null);
        userRepository.save(user);

        // Ta bort hushållet om tomt
        if (household != null && (household.getMembers() == null || household.getMembers().isEmpty())) {
            householdRepository.deleteById(householdId);
        }

        return ResponseEntity.status(HttpStatus.OK).body("User with ID " + userID + " has left household " + householdId);
    }

    /* -- Hämta hushåll -- */
    public ResponseEntity<Object> getHousehold(String userID) {
        // Kolla om användaren är valid och med i ett hushåll
        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userID + " not found");
        } else if (userOptional.get().getHouseholdId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with ID " + userID + " is not in a household");
            
        }

        // Hämta hushållet
        Household household = householdRepository.findById(userOptional.get().getHouseholdId()).orElse(null);
        if (household == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Household not found");
        }

        return ResponseEntity.ok(household);
    }

    /* -- Nollställ poäng för alla medlemmar i hushållet -- */
    public ResponseEntity<Object> resetScores(String householdId, String userId) {
        // Kolla om användaren är valid och har rätt att nollställa poäng
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userId + " not found");
        }

        // Kolla om hushållet finns
        Optional<Household> householdOptional = householdRepository.findById(householdId);
        if (householdOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Household with ID " + householdId + " not found");
        }

        // Kolla om användaren är ägare av hushållet
        Household household = householdOptional.get();
        if (!household.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User with ID " + userId + " is not the owner of household " + householdId);
        }

        // Nollställ poäng för alla medlemmar
        household.getMembers().forEach(member -> member.setScore(0));
        householdRepository.save(household);

        return ResponseEntity.ok("Scores reset for all members of household " + householdId);
    }

}
