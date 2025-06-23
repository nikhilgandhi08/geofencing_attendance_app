package com.example.app006.employee.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.app006.R;
import com.example.app006.models.AdminEmp;
import com.example.app006.models.GeofenceModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class LiveTrackingFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btnRefresh, btnMarkIn, btnMarkOut;
    private TextView locationStatus;
    private SharedPreferences prefs;
    private String currentEmail;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private LatLng geofenceCenter = null;
    private float geofenceRadius = 0f;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private boolean locationPermissionGranted = false;
    private Location lastKnownLocation = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.employee_live_tracking, container, false);

        prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentEmail = prefs.getString("email", null);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnRefresh = rootView.findViewById(R.id.btn_refresh_location);
        btnMarkIn = rootView.findViewById(R.id.btn_mark_in);
        btnMarkOut = rootView.findViewById(R.id.btn_mark_out);
        locationStatus = rootView.findViewById(R.id.location_status);

        // Disable Mark In/Out until geofence and location ready
        btnMarkIn.setEnabled(false);
        btnMarkOut.setEnabled(false);

        btnRefresh.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                refreshLocation();
            }
        });

        btnMarkIn.setOnClickListener(v -> markAttendance("login"));
        btnMarkOut.setOnClickListener(v -> markAttendance("logout"));

        // Check permission and start loading chain
        if (checkLocationPermission()) {
            loadAdminEmailForEmployee(currentEmail);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        return rootView;
    }

    private boolean checkLocationPermission() {
        locationPermissionGranted = ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return locationPermissionGranted;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    private void loadAdminEmailForEmployee(String empEmail) {
        if (empEmail == null) {
            Toast.makeText(getContext(), "User email not found", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("hr-employees")
                .whereEqualTo("empEmail", empEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = queryDocumentSnapshots.iterator().next();
                        AdminEmp adminEmp = doc.toObject(AdminEmp.class);
                        String adminEmail = adminEmp.getAdminEmail();
                        loadGeofenceForAdmin(adminEmail);
                    } else {
                        Toast.makeText(getContext(), "Admin not found for employee", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch admin email", Toast.LENGTH_SHORT).show();
                    Log.e("LiveTrackingFragment", "Error fetching admin email", e);
                });
    }


    private void loadGeofenceForAdmin(String adminEmail) {
        if (adminEmail == null) {
            Toast.makeText(getContext(), "Admin email is null", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("set-geofence")
                .whereEqualTo("email", adminEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = queryDocumentSnapshots.iterator().next();
                        GeofenceModel geofence = doc.toObject(GeofenceModel.class);
                        geofenceCenter = new LatLng(geofence.getLatitude(), geofence.getLongitude());
                        geofenceRadius = (float) geofence.getRadius();
                        Toast.makeText(getContext(), "Geofence loaded", Toast.LENGTH_SHORT).show();

                        // Enable refresh and mark buttons now
                        btnMarkIn.setEnabled(true);
                        btnMarkOut.setEnabled(true);

                        refreshLocation();
                    } else {
                        Toast.makeText(getContext(), "Geofence not found for admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch geofence", Toast.LENGTH_SHORT).show();
                    Log.e("LiveTrackingFragment", "Error fetching geofence", e);
                });
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);
        refreshLocation();
    }

    @SuppressLint("MissingPermission")
    private void refreshLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        locationStatus.setText("Fetching current location...");

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null && mMap != null) {
                lastKnownLocation = location;
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(current).title("You are here"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 16));

                if (geofenceCenter != null && geofenceRadius > 0) {
                    mMap.addCircle(new CircleOptions()
                            .center(geofenceCenter)
                            .radius(geofenceRadius)
                            .strokeColor(Color.BLUE)
                            .fillColor(0x220000FF)
                            .strokeWidth(4f));
                }

                fetchAddressFromCoordinates(location.getLatitude(), location.getLongitude());
            } else {
                locationStatus.setText("Unable to fetch location");
                Toast.makeText(getContext(), "Location is null. Try refreshing.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            locationStatus.setText("Failed to fetch location");
            Toast.makeText(getContext(), "Failed to fetch location", Toast.LENGTH_SHORT).show();
            Log.e("LiveTrackingFragment", "Error fetching location", e);
        });
    }

    private void fetchAddressFromCoordinates(double latitude, double longitude) {
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), Locale.getDefault());
                java.util.List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    String addressString = formatAddress(address);

                    requireActivity().runOnUiThread(() -> {
                        locationStatus.setText(addressString);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        locationStatus.setText("Address not found");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    locationStatus.setText("Failed to get address");
                });
            }
        }).start();
    }

    private String formatAddress(android.location.Address address) {
        StringBuilder sb = new StringBuilder();

        if (address.getFeatureName() != null) {
            sb.append(address.getFeatureName()).append(", ");
        }
        if (address.getSubLocality() != null) {
            sb.append(address.getSubLocality()).append(", ");
        }
        if (address.getLocality() != null) {
            sb.append(address.getLocality()).append(", ");
        }
        if (address.getAdminArea() != null) {
            sb.append(address.getAdminArea()).append(" ");
        }
        if (address.getPostalCode() != null) {
            sb.append(address.getPostalCode());
        }

        return sb.toString().trim();
    }

    private void markAttendance(String type) {
        if (!checkLocationPermission()) {
            Toast.makeText(getContext(), "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lastKnownLocation == null) {
            Toast.makeText(getContext(), "Current location unknown. Please refresh.", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = lastKnownLocation.getLatitude();
        double lng = lastKnownLocation.getLongitude();

        boolean inside = false;
        if (geofenceCenter != null && geofenceRadius > 0) {
            float[] dist = new float[1];
            Location.distanceBetween(lat, lng,
                    geofenceCenter.latitude, geofenceCenter.longitude, dist);
            inside = dist[0] <= geofenceRadius;
        }

        if (!inside) {
            Toast.makeText(getContext(), "You are outside the geofence area. Attendance not marked.", Toast.LENGTH_LONG).show();
            return;
        }

        saveAttendance(type, lat, lng, inside);
    }

    private void saveAttendance(String type, double lat, double lng, boolean inside) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("email", currentEmail);
        data.put("type", type);
        data.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        data.put("time", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
        data.put("latitude", lat);
        data.put("longitude", lng);
        data.put("insideGeofence", inside);

        db.collection("attendance-data")
                .add(data)
                .addOnSuccessListener(doc -> Toast.makeText(getContext(),
                        "Attendance marked successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to mark attendance", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                if (mMap != null) {
                    onMapReady(mMap);
                }
                loadAdminEmailForEmployee(currentEmail);
            } else {
                Toast.makeText(getContext(), "Location permission denied. Enable location for attendance.", Toast.LENGTH_LONG).show();
                btnMarkIn.setEnabled(false);
                btnMarkOut.setEnabled(false);
            }
        }
    }
}


//package com.example.app006.employee.Fragments;




//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.app.ActivityCompat;
//import androidx.fragment.app.Fragment;
//
//import com.example.app006.R;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.CircleOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Locale;
//
//public class LiveTrackingFragment extends Fragment implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//    private FusedLocationProviderClient fusedLocationClient;
//    private Button btnRefresh, btnMarkIn, btnMarkOut;
//    private TextView locationStatus;
//    private SharedPreferences prefs;
//    private String currentEmail;
//
//    private static final LatLng GEOFENCE_CENTER = new LatLng(37.4220, -122.0841);
//    private static final float GEOFENCE_RADIUS = 200; // meters
//    private static final String PREFS_NAME = "LoginPrefs";
//    private static final String KEY_LAST_LOGIN = "last_login_time_";
//    private static final String KEY_LAST_LOGOUT = "last_logout_time_";
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.employee_live_tracking, container, false);
//
//        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
//        currentEmail = sharedPreferences.getString("email", "johndoe@example.com");
//
//        SupportMapFragment mapFragment = (SupportMapFragment)
//                getChildFragmentManager().findFragmentById(R.id.map);
//        if (mapFragment != null) mapFragment.getMapAsync(this);
//
//        btnRefresh = rootView.findViewById(R.id.btn_refresh_location);
//        btnMarkIn = rootView.findViewById(R.id.btn_mark_in);
//        btnMarkOut = rootView.findViewById(R.id.btn_mark_out);
//        locationStatus = rootView.findViewById(R.id.location_status);
//
//        btnRefresh.setOnClickListener(v -> refreshLocation());
//        btnMarkIn.setOnClickListener(v -> {
//            markAttendance("login");
//            disableButton(btnMarkIn, KEY_LAST_LOGIN + currentEmail);
//        });
//        btnMarkOut.setOnClickListener(v -> {
//            markAttendance("logout");
//            disableButton(btnMarkOut, KEY_LAST_LOGOUT + currentEmail);
//        });
//
//        setupButtonState(btnMarkIn, KEY_LAST_LOGIN + currentEmail, "login");
//        setupButtonState(btnMarkOut, KEY_LAST_LOGOUT + currentEmail, "logout");
//
//        return rootView;
//    }
//
//    @SuppressLint("MissingPermission")
//    @Override
//    public void onMapReady(@NonNull GoogleMap googleMap) {
//        mMap = googleMap;
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(requireActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            return;
//        }
//        mMap.setMyLocationEnabled(true);
//        refreshLocation();
//    }
//
//    private void setupButtonState(Button button, String key, String type) {
//        long lastTime = prefs.getLong(key, 0);
//        if (lastTime == 0) return;
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(lastTime);
//        calendar.add(Calendar.DATE, 1); // next day
//
//        if (type.equals("login")) {
//            calendar.set(Calendar.HOUR_OF_DAY, 9);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//        } else if (type.equals("logout")) {
//            calendar.set(Calendar.HOUR_OF_DAY, 17);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//        }
//
//        if (System.currentTimeMillis() < calendar.getTimeInMillis()) {
//            button.setEnabled(false);
//        } else {
//            button.setEnabled(true);
//        }
//    }
//
//    private void disableButton(Button button, String key) {
//        prefs.edit().putLong(key, System.currentTimeMillis()).apply();
//        button.setEnabled(false);
//    }
//
//    private void refreshLocation() {
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(requireActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            return;
//        }
//        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
//            if (location != null && mMap != null) {
//                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.clear();
//                mMap.addMarker(new MarkerOptions().position(current).title("You are here"));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 16));
//                mMap.addCircle(new CircleOptions()
//                        .center(GEOFENCE_CENTER)
//                        .radius(GEOFENCE_RADIUS)
//                        .strokeColor(Color.BLUE)
//                        .fillColor(0x220000FF)
//                        .strokeWidth(4f));
//                locationStatus.setText(String.format(Locale.getDefault(),
//                        "Location: %.5f, %.5f", current.latitude, current.longitude));
//            }
//        });
//    }
//
//    private void markAttendance(String type) {
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getContext(), "Permission required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
//            if (location == null) {
//                Toast.makeText(getContext(), "Location unavailable", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            double lat = location.getLatitude();
//            double lng = location.getLongitude();
//            float[] dist = new float[1];
//            Location.distanceBetween(lat, lng,
//                    GEOFENCE_CENTER.latitude, GEOFENCE_CENTER.longitude, dist);
//            boolean inside = dist[0] <= GEOFENCE_RADIUS;
//            saveAttendance(type, lat, lng, inside);
//        });
//    }
//
//    private void saveAttendance(String type, double lat, double lng, boolean inside) {
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("email", currentEmail);
//        data.put("type", type);
//        data.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
//        data.put("time", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
//        data.put("latitude", lat);
//        data.put("longitude", lng);
//        data.put("insideGeofence", inside);
//        FirebaseFirestore.getInstance()
//                .collection("attendance-data")
//                .add(data)
//                .addOnSuccessListener(doc -> Toast.makeText(getContext(),
//                        "Attendance marked", Toast.LENGTH_SHORT).show())
//                .addOnFailureListener(e -> Toast.makeText(getContext(),
//                        "Failed to mark attendance", Toast.LENGTH_SHORT).show());
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == 1 && grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            if (mMap != null) onMapReady(mMap);
//        }
//    }
//}
