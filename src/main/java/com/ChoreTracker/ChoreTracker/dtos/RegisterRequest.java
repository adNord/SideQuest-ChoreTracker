package com.ChoreTracker.ChoreTracker.dtos;

public record RegisterRequest(String username, String password, String passwordConfirm) {

    public boolean passwordsMatch() {
        return password.equals(passwordConfirm);
    }
} 
