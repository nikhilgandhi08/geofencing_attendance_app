package com.example.app006.services;

import android.location.Location;

public class GeofenceUtils {
    // Office or predefined geofence location (Example: New York)
    private static final double OFFICE_LAT = 40.7128;
    private static final double OFFICE_LON = -74.0060;
    private static final float GEOFENCE_RADIUS = 500; // 500 meters

    /**
     * Checks if the given coordinates are inside the geofenced area.
     *
     * @param lat User's latitude
     * @param lon User's longitude
     * @return true if inside geofence, false otherwise
     */
    public static boolean isInsideGeofence(double lat, double lon) {
        float[] results = new float[1];
        Location.distanceBetween(lat, lon, OFFICE_LAT, OFFICE_LON, results);
        return results[0] <= GEOFENCE_RADIUS;
    }
}
