package com.example.app006.models;

public class LeaderboardEmployee {
    private String name;
    private String email;
    private int loginCount;

    public LeaderboardEmployee(String name, String email, int loginCount) {
        this.name = name;
        this.email = email;
        this.loginCount = loginCount;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getLoginCount() {
        return loginCount;
    }
}
