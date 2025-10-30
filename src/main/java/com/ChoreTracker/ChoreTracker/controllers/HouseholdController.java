package com.ChoreTracker.ChoreTracker.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ChoreTracker.ChoreTracker.dtos.household.CreateHouseholdRequest;
import com.ChoreTracker.ChoreTracker.dtos.household.JoinHouseholdRequest;
import com.ChoreTracker.ChoreTracker.dtos.household.LeaveHouseholdRequest;
import com.ChoreTracker.ChoreTracker.models.UserPrincipal;
import com.ChoreTracker.ChoreTracker.service.HouseholdService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/household")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @PostMapping
    public ResponseEntity<Object> postNewHousehold(@RequestBody CreateHouseholdRequest householdRequest, Authentication authentication) {
        String userId = getUserId(authentication);

        return householdService.createHousehold(householdRequest, userId);
    }

    @PostMapping("/join")
    public ResponseEntity<Object> postJoinHousehold(@RequestBody JoinHouseholdRequest joinRequest, Authentication authentication) {
        String userId = getUserId(authentication);

        return householdService.joinHousehold(joinRequest.inviteCode(), userId);
    }
    
    @PostMapping("/leave")
    public ResponseEntity<Object> postLeaveHousehold(@RequestBody LeaveHouseholdRequest leaveRequest, Authentication authentication) {
        String userId = getUserId(authentication);

        return householdService.leaveHousehold(leaveRequest.householdId(), userId);
    }

    @GetMapping
    public ResponseEntity<Object> getHousehold(Authentication authentication) {
        String userId = getUserId(authentication);

        return householdService.getHousehold(userId);
    }

    @PatchMapping("/{householdId}/reset-scores")
    public ResponseEntity<Object> resetScores(@PathVariable String householdId, Authentication authentication) {
        String userId = getUserId(authentication);

        return householdService.resetScores(householdId, userId);
    }

    private String getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) return up.getId();
        return "";
    }
    
    
}
