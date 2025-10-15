package com.ChoreTracker.ChoreTracker.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/household")
public class HouseholdController {
    
    @PostMapping
    public ResponseEntity<Object> postNewHousehold(@RequestBody String entity) {
        //TODO: process POST request

        return ResponseEntity.ok(entity);
    }

    @PostMapping("/join/")
    public ResponseEntity<Object> postJoinHousehold(@RequestBody String code) {
        //TODO: process POST request

        return ResponseEntity.ok(code);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getHousehold(@PathVariable String id, @RequestBody String entity) {
        //TODO: process GET request

        return ResponseEntity.ok(entity);
    }
    
    @PostMapping("/leave/{id}")
    public ResponseEntity<Object> postLeaveHousehold(@PathVariable String id, @RequestBody String entity) {
        //TODO: process POST request

        return ResponseEntity.ok(entity);
    }
    
    
}
