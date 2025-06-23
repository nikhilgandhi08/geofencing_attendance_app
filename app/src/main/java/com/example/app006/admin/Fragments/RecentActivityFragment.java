package com.example.app006.admin.Fragments;

import androidx.recyclerview.widget.RecyclerView;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app006.R;
import com.example.app006.adapters.RecentActivityAdapter;
import com.example.app006.models.ActivityItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecentActivityFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecentActivityAdapter adapter;
    private List<ActivityItem> activityList = new ArrayList<>();
    private FirebaseFirestore db;

    private Button btnSelectDate;
    private TextView tvSelectedDate;
    private Calendar selectedCalendar;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String adminEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_dashbaord_fragment_recent_activity, container, false);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        adminEmail = prefs.getString("email", "admin@example.com");

        if (adminEmail == null) {
            Toast.makeText(getContext(), "Admin not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        recyclerView = view.findViewById(R.id.rv_all_activities);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnSelectDate = view.findViewById(R.id.btnSelectDate);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        selectedCalendar = Calendar.getInstance();

        adapter = new RecentActivityAdapter(activityList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Load today's data by default
        String todayDate = dateFormat.format(selectedCalendar.getTime());
        tvSelectedDate.setText("Showing: " + todayDate);
        loadActivitiesFromFirestore(todayDate);

        // Button click to open calendar
        btnSelectDate.setOnClickListener(v -> openDatePicker());

        return view;
    }

    private void openDatePicker() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (DatePicker view, int y, int m, int d) -> {
                    selectedCalendar.set(y, m, d);
                    String selectedDate = dateFormat.format(selectedCalendar.getTime());
                    tvSelectedDate.setText("Showing: " + selectedDate);
                    loadActivitiesFromFirestore(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadActivitiesFromFirestore(String selectedDate) {
        db.collection("hr-employees")
                .whereEqualTo("adminEmail", adminEmail)
                .get()
                .addOnSuccessListener(hrSnapshot -> {
                    List<String> employeeEmails = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : hrSnapshot) {
                        String empEmail = doc.getString("empEmail");
                        if (empEmail != null) {
                            employeeEmails.add(empEmail);
                        }
                    }

                    if (employeeEmails.isEmpty()) {
                        activityList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "No linked employees found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    activityList.clear();

                    // Firestore whereIn supports max 10 values — batching
                    List<List<String>> batches = new ArrayList<>();
                    for (int i = 0; i < employeeEmails.size(); i += 10) {
                        batches.add(employeeEmails.subList(i, Math.min(i + 10, employeeEmails.size())));
                    }

                    for (List<String> batch : batches) {
                        db.collection("attendance-data")
                                .whereEqualTo("date", selectedDate)
                                .whereIn("email", batch)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    for (QueryDocumentSnapshot doc : snapshot) {
                                        String email = doc.getString("email");
                                        String type = doc.getString("type");
                                        String time = doc.getString("time");
                                        String date = doc.getString("date");
                                        Boolean insideGeofence = doc.getBoolean("insideGeofence");

                                        ActivityItem item = new ActivityItem(
                                                email != null ? email : "Unknown",
                                                type != null ? type : "Unknown",
                                                time != null ? time : "--:--",
                                                date != null ? date : selectedDate,
                                                insideGeofence != null && insideGeofence
                                        );
                                        activityList.add(item);
                                    }
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Failed to load attendance", Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to fetch employees", Toast.LENGTH_SHORT).show()
                );
    }
}