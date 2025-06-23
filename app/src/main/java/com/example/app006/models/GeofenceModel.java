package com.example.app006.models;

public class GeofenceModel {

    private double latitude;
    private double longitude;
    private String email;
    private int radius;
    private String geofenceId;

    public GeofenceModel() {
        // Default constructor required for Firestore
    }

    public GeofenceModel(double latitude, double longitude, String email, int radius, String geofenceId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.email = email;
        this.radius = radius;
        this.geofenceId = geofenceId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getGeofenceId() {
        return geofenceId;
    }

    public void setGeofenceId(String geofenceId) {
        this.geofenceId = geofenceId;
    }
}
