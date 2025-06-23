package com.example.app006.models;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmployeeLocationDatabase {
    private final FirebaseFirestore db;

    public EmployeeLocationDatabase() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveLocation(String username, double latitude, double longitude, boolean insideGeofence) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("username", username);
        locationData.put("date", date);
        locationData.put("time", time);
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("insideGeofence", insideGeofence);

        db.collection("employee-location").add(locationData)
                .addOnSuccessListener(documentReference -> Log.d("DB", "Location saved"))
                .addOnFailureListener(e -> Log.e("DB", "Error saving location", e));
    }
}
