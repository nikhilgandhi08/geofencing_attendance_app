package com.example.app006.admin.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.app006.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DashboardFragment extends Fragment {

    private TextView textWelcomeMessage, textAdminName, textCompanyName;
    private TextView textTotalEmployees, textPresentToday, textPendingLeaves;

    private FirebaseFirestore db;
    private String adminEmail;

    private TextView tvAttendanceRate, tvAvgWorkHours;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_fragment_dashboard, container, false);

        // Initialize views
        textWelcomeMessage = view.findViewById(R.id.tv_admin_welcome_message);
        textAdminName = view.findViewById(R.id.tv_admin_name);
        textCompanyName = view.findViewById(R.id.tv_company_name);

        textTotalEmployees = view.findViewById(R.id.tv_total_employees);
        textPresentToday = view.findViewById(R.id.tv_present_today);
        textPendingLeaves = view.findViewById(R.id.tv_pending_leaves);


        tvAttendanceRate = view.findViewById(R.id.tv_attendance_rate);
        tvAvgWorkHours = view.findViewById(R.id.tv_avg_work_hours);

        // --- Add your "See All" TextView click listener here ---
        TextView tvSeeAll = view.findViewById(R.id.tv_see_all_activity);
        tvSeeAll.setOnClickListener(v -> {
            FragmentTransaction ft = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            ft.replace(R.id.fragment_container, new RecentActivityFragment());
            ft.addToBackStack(null);
            ft.commit();
        });

        db = FirebaseFirestore.getInstance();

        // Load admin info
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        adminEmail = prefs.getString("email", "admin@example.com");
        String name = prefs.getString("name", "Admin");
        String company = prefs.getString("companyName", "Company Name");

        textAdminName.setText(name);
        textCompanyName.setText(company);

        setDynamicGreeting();

        // Load stats
        loadTotalEmployees();
        loadPresentToday();
        loadPendingLeaves();
        loadQuickStats();

        // Set click listeners for Admin Actions
        view.findViewById(R.id.action_employee_management).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new EmployeeManagementFragment())
                        .addToBackStack(null)
                        .commit());

        view.findViewById(R.id.action_attendance_monitoring).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new DashboardFragment()) // You can replace with AttendanceMonitoringFragment if created separately
                        .addToBackStack(null)
                        .commit());

        view.findViewById(R.id.action_leave_management).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new LeaveManagementFragment())
                        .addToBackStack(null)
                        .commit());

        view.findViewById(R.id.action_geofence_settings).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SetGeofence())
                        .addToBackStack(null)
                        .commit());

        view.findViewById(R.id.action_performance_tests).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new PayrollFragment())
                        .addToBackStack(null)
                        .commit());


        view.findViewById(R.id.action_reports).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ReportsFragment())
                        .addToBackStack(null)
                        .commit());



        return view;
    }

    private void setDynamicGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
        }

        textWelcomeMessage.setText(greeting + ", Admin");
    }

    private void loadTotalEmployees() {
        db.collection("hr-employees")
                .whereEqualTo("adminEmail", adminEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalEmployees = queryDocumentSnapshots.size();
                    textTotalEmployees.setText(String.valueOf(totalEmployees));
                })
                .addOnFailureListener(e -> textTotalEmployees.setText("0"));
    }

    private void loadPresentToday() {
        // First get list of employee emails linked to admin
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
                        textPresentToday.setText("0");
                        return;
                    }

                    // Get today's date string
                    String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

                    // Query attendance-data for type=login, date=today, email in employeeEmails
                    db.collection("attendance-data")
                            .whereEqualTo("type", "login")
                            .whereEqualTo("date", todayDate)
                            .whereIn("email", employeeEmails)
                            .get()
                            .addOnSuccessListener(attendanceSnapshot -> {
                                // Use a Set to store unique emails
                                Set<String> uniqueEmails = new HashSet<>();
                                for (QueryDocumentSnapshot doc : attendanceSnapshot) {
                                    String email = doc.getString("email");
                                    if (email != null) {
                                        uniqueEmails.add(email);
                                    }
                                }
                                int uniqueCount = uniqueEmails.size();
                                textPresentToday.setText(String.valueOf(uniqueCount));
                            })
                            .addOnFailureListener(e -> textPresentToday.setText("0"));

                })
                .addOnFailureListener(e -> textPresentToday.setText("0"));
    }


    private void loadPendingLeaves() {
        // Get employees linked to admin
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
                        textPendingLeaves.setText("0");
                        return;
                    }

                    // Query leave-requests where employeeEmail in employeeEmails and status = pending
                    db.collection("leave-requests")
                            .whereEqualTo("status", "pending")
                            .whereIn("employeeEmail", employeeEmails)
                            .get()
                            .addOnSuccessListener(leaveSnapshot -> {
                                int pendingCount = leaveSnapshot.size();
                                textPendingLeaves.setText(String.valueOf(pendingCount));
                            })
                            .addOnFailureListener(e -> textPendingLeaves.setText("0"));
                })
                .addOnFailureListener(e -> textPendingLeaves.setText("0"));
    }


    private void loadQuickStats() {
        db.collection("hr-employees")
                .whereEqualTo("adminEmail", adminEmail)
                .get()
                .addOnSuccessListener(hrSnapshot -> {
                    List<String> empEmails = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : hrSnapshot) {
                        empEmails.add(doc.getString("empEmail"));
                    }

                    int totalEmployees = empEmails.size();

                    if (totalEmployees == 0) {
                        tvAttendanceRate.setText("0%");
                        tvAvgWorkHours.setText("0 hrs");
                        return;
                    }

                    String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Calendar.getInstance().getTime());

                    db.collection("attendance-data")
                            .whereEqualTo("date", todayDate)
                            .whereEqualTo("type", "login")
                            .whereIn("email", empEmails)
                            .get()
                            .addOnSuccessListener(attSnapshot -> {
                                // Attendance Rate
                                Set<String> uniqueEmails = new HashSet<>();
                                for (QueryDocumentSnapshot doc : attSnapshot) {
                                    String email = doc.getString("email");
                                    if (email != null) {
                                        uniqueEmails.add(email);
                                    }
                                }
                                int presentCount = uniqueEmails.size();
                                int attendanceRate = (presentCount * 100) / totalEmployees;
                                tvAttendanceRate.setText(attendanceRate + "%");


                                // Now calculate Avg Work Hours
                                db.collection("attendance-data")
                                        .whereIn("email", empEmails)
                                        .get()
                                        .addOnSuccessListener(allAttSnapshot -> {
                                            HashMap<String, List<QueryDocumentSnapshot>> logsGrouped = new HashMap<>();

                                            for (QueryDocumentSnapshot doc : allAttSnapshot) {
                                                String email = doc.getString("email");
                                                String date = doc.getString("date");
                                                String key = email + "_" + date;

                                                if (!logsGrouped.containsKey(key)) {
                                                    logsGrouped.put(key, new ArrayList<>());
                                                }
                                                logsGrouped.get(key).add(doc);
                                            }

                                            long totalMillis = 0;
                                            int dayCount = 0;

                                            for (List<QueryDocumentSnapshot> logs : logsGrouped.values()) {
                                                Date earliestLogin = null;
                                                Date latestLogout = null;

                                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

                                                for (QueryDocumentSnapshot log : logs) {
                                                    String type = log.getString("type");
                                                    String timeStr = log.getString("time");
                                                    if (timeStr == null) continue;

                                                    try {
                                                        Date time = sdf.parse(timeStr);

                                                        if ("login".equalsIgnoreCase(type)) {
                                                            if (earliestLogin == null || time.before(earliestLogin)) {
                                                                earliestLogin = time;
                                                            }
                                                        } else if ("logout".equalsIgnoreCase(type)) {
                                                            if (latestLogout == null || time.after(latestLogout)) {
                                                                latestLogout = time;
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                if (earliestLogin != null && latestLogout != null && latestLogout.after(earliestLogin)) {
                                                    totalMillis += (latestLogout.getTime() - earliestLogin.getTime());
                                                    dayCount++;
                                                }
                                            }


                                            double avgHours = (dayCount > 0)
                                                    ? totalMillis / (dayCount * 3600000.0)
                                                    : 0;
                                            tvAvgWorkHours.setText(String.format(Locale.getDefault(), "%.2f hrs", avgHours));
                                        });

                            });
                });
    }



}