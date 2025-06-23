package com.example.app006.models;

public class ActivityItem {
    private String email;
    private String type;
    private String time;
    private String date;
    private boolean insideGeofence;

    public ActivityItem(String email, String type, String time, String date, boolean insideGeofence) {
        this.email = email;
        this.type = type;
        this.time = time;
        this.date = date;
        this.insideGeofence = insideGeofence;
    }

    public String getEmail() { return email; }
    public String getType() { return type; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public boolean isInsideGeofence() { return insideGeofence; }
}

