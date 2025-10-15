package com.ChoreTracker.ChoreTracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ChoreTracker.ChoreTracker.dtos.CreateHouseholdRequest;
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
        System.out.println("Joining household with invite code " + inviteCode + " for user " + userID);
        //Kolla om användaren är valid och inte med i ett hushåll redan
        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + userID + " not found");
        } else if (userOptional.get().getHouseholdId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with ID " + userID + " already in a household");
        }
        System.out.println("User is valid and not in a household");

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

        return ResponseEntity.status(HttpStatus.OK).body("Joining household with invite code " + inviteCode + " for user " + userID);
    }

}
