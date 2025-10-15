package com.ChoreTracker.ChoreTracker.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ChoreTracker.ChoreTracker.dtos.CreateHouseholdRequest;
import com.ChoreTracker.ChoreTracker.dtos.JoinHouseholdRequest;
import com.ChoreTracker.ChoreTracker.models.UserPrincipal;
import com.ChoreTracker.ChoreTracker.service.HouseholdService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
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
        String userID = getUserId(authentication);

        return householdService.createHousehold(householdRequest, userID);
    }

    @PostMapping("/join")
    public ResponseEntity<Object> postJoinHousehold(@RequestBody JoinHouseholdRequest joinRequest, Authentication authentication) {
        String userID = getUserId(authentication);

        return householdService.joinHousehold(joinRequest.inviteCode(), userID);
    }
    
    @PostMapping("/leave")
    public ResponseEntity<Object> postLeaveHousehold(@PathVariable String id, @RequestBody String entity) {
        //TODO: process POST request

        return ResponseEntity.ok(entity);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getHousehold(@PathVariable String id, @RequestBody String entity) {
        //TODO: process GET request

        return ResponseEntity.ok(entity);
    }

    private String getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) return up.getId();
        return "";
    }
    
    
}
