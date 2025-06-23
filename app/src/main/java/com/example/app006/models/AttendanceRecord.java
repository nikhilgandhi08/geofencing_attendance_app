package com.example.app006.models;

public class AttendanceRecord {
    private String employeeId;  // Unique employee ID
    private String date;        // Date of record (YYYY-MM-DD)
    private String timestamp;   // Timestamp (HH:mm)
    private double latitude;    // Employee's latitude
    private double longitude;   // Employee's longitude
    private boolean insideGeofence; // True if inside the geofenced area
    private String remarks;     // Attendance status (Late, Early Exit, etc.)

    public AttendanceRecord() {
        // Default constructor
    }

    public AttendanceRecord(String employeeId, String date, String timestamp, double latitude, double longitude, boolean insideGeofence, String remarks) {
        this.employeeId = employeeId;
        this.date = date;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.insideGeofence = insideGeofence;
        this.remarks = remarks;
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isInsideGeofence() {
        return insideGeofence;
    }

    public void setInsideGeofence(boolean insideGeofence) {
        this.insideGeofence = insideGeofence;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
