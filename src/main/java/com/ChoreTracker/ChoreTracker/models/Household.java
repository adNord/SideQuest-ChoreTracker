package com.ChoreTracker.ChoreTracker.models;

import java.util.List;

import org.springframework.data.annotation.Id;
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
        private String memberId;
        private int score;

        public MemberScore(String memberId, int score) {
            this.memberId = memberId;
            this.score = score;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
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
