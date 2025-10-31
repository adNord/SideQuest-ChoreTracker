package com.ChoreTracker.ChoreTracker.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "households")
public class Household {

    @Id
    private String id;
    private String name;
    private String ownerId;
    private List<MemberScore> members;
    private String inviteCode;
    private boolean isPremium;

    public Household(String name, String ownerId, List<MemberScore> members, String inviteCode, boolean isPremium) {
        this.name = name;
        this.ownerId = ownerId;
        this.members = members;
        this.inviteCode = inviteCode;
        this.isPremium = isPremium;
    }

    public Household() {}
    
    public static class MemberScore {
        @DBRef
        private User user;
        private int score;

        public MemberScore(User user, int score) {
            this.user = user;
            this.score = score;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public void incrementScore(int increment) {
            this.score += increment;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<MemberScore> getMembers() {
        return members;
    }

    public void setMembers(List<MemberScore> members) {
        this.members = members;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean isPremium) {
        this.isPremium = isPremium;
    }
}
