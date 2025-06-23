package com.example.app006.admin.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.app006.R;
import com.example.app006.models.GeofenceModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class SetGeofence extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private boolean isMapReady = false;
    private EditText radiusInput;
    private Button saveButton, deleteButton;
    private LatLng selectedLocation;
    private FirebaseFirestore db;
    private String adminEmail;
    private String existingGeofenceId;
    private boolean geofenceExists = false;
    private GeofenceModel currentGeofence;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_set_geofence, container, false);

        // Initialize views
        mapView = view.findViewById(R.id.mapView);
        radiusInput = view.findViewById(R.id.radius_input);
        saveButton = view.findViewById(R.id.save_button);
        deleteButton = view.findViewById(R.id.delete_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load admin email
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        adminEmail = prefs.getString("email", null);

        // Initialize MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Hide delete by default
        deleteButton.setVisibility(View.GONE);

        // Check existing geofence first
        if (adminEmail != null) {
            db.collection("set-geofence")
                    .whereEqualTo("email", adminEmail)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            currentGeofence = query.getDocuments().get(0)
                                    .toObject(GeofenceModel.class);
                            if (currentGeofence != null) {
                                geofenceExists = true;
                                existingGeofenceId = currentGeofence.getGeofenceId();
                                selectedLocation = new LatLng(
                                        currentGeofence.getLatitude(),
                                        currentGeofence.getLongitude());
                                radiusInput.setText(String.valueOf(currentGeofence.getRadius()));
                                saveButton.setText("Update");
                                deleteButton.setVisibility(View.VISIBLE);
                                // Draw immediately if map is ready
                                if (isMapReady) {
                                    drawGeofenceOnMap(selectedLocation, currentGeofence.getRadius());
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(
                            requireContext(), "Error checking geofence", Toast.LENGTH_SHORT
                    ).show());
        }

        // Button actions
        saveButton.setOnClickListener(v -> saveOrUpdateGeofence());
        deleteButton.setOnClickListener(v -> deleteGeofence());

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        isMapReady = true;

        // If a geofence exists, draw it
        if (geofenceExists && selectedLocation != null) {
            drawGeofenceOnMap(selectedLocation, currentGeofence.getRadius());
        }

        // Handle new location selection
        googleMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            drawGeofenceOnMap(latLng, null); // no radius until saved
        });
    }

    private void drawGeofenceOnMap(@NonNull LatLng location, Integer radius) {
        if (!isMapReady) return;

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Geofence Location"));

        if (radius != null) {
            googleMap.addCircle(new CircleOptions()
                    .center(location)
                    .radius(radius)
                    .strokeColor(0xFFFF0000)
                    .fillColor(0x44FF0000)
                    .strokeWidth(5));
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private void saveOrUpdateGeofence() {
        if (selectedLocation == null) {
            Toast.makeText(requireContext(), "Select a location", Toast.LENGTH_SHORT).show();
            return;
        }
        String radText = radiusInput.getText().toString().trim();
        if (radText.isEmpty()) {
            Toast.makeText(requireContext(), "Enter radius", Toast.LENGTH_SHORT).show();
            return;
        }

        int radius = Integer.parseInt(radText);
        String geofenceId = geofenceExists ? existingGeofenceId : db.collection("set-geofence").document().getId();

        GeofenceModel gf = new GeofenceModel(
                selectedLocation.latitude,
                selectedLocation.longitude,
                adminEmail,
                radius,
                geofenceId
        );

        db.collection("set-geofence")
                .document(geofenceId)
                .set(gf)
                .addOnSuccessListener(aVoid -> {
                    geofenceExists = true;
                    existingGeofenceId = geofenceId;
                    currentGeofence = gf;
                    saveButton.setText("Update");
                    deleteButton.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(),
                            geofenceExists ? "Geofence updated" : "Geofence saved",
                            Toast.LENGTH_SHORT).show();
                    drawGeofenceOnMap(selectedLocation, radius);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        requireContext(), "Save failed", Toast.LENGTH_SHORT).show());
    }

    private void deleteGeofence() {
        if (!geofenceExists) {
            Toast.makeText(requireContext(), "Nothing to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("set-geofence")
                .document(existingGeofenceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    geofenceExists = false;
                    existingGeofenceId = null;
                    selectedLocation = null;
                    radiusInput.setText("");
                    googleMap.clear();
                    saveButton.setText("Save");
                    deleteButton.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Geofence removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        requireContext(), "Delete failed", Toast.LENGTH_SHORT).show());
    }

    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
}
