package com.example.app006.models;

import java.util.Date;

public class AttendanceRec {
    public String date;
    public Date loginTime;
    public Date logoutTime;
    public boolean insideGeofence;

    public AttendanceRec(String date, Date loginTime, Date logoutTime, boolean insideGeofence) {
        this.date = date;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.insideGeofence = insideGeofence;
    }
    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Date getLogoutTime() {
        return logoutTime;
    }
    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }

    public boolean isInsideGeofence() {
        return insideGeofence;
    }
    public void setInsideGeofence(boolean insideGeofence) {
        this.insideGeofence = insideGeofence;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    // Add constructor, getters, and setters
}

