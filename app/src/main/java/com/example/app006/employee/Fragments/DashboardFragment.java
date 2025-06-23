package com.example.app006.employee.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.app006.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class DashboardFragment extends Fragment {

    private TextView textWelcomeMessage, textUserName, textWeeklyHours, textMonthlyHours;
    private ImageView profilePicture;
    private TextView textStatus, textLastCheckedIn, textLeaveStatus;

    private FirebaseFirestore db;

    Context context = getContext();

    private View statusDotView;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.employee_fragment_dashboard, container, false);
        MaterialButton btnCheckIn = view.findViewById(R.id.btn_check_in);

        // Initialize views
        textWelcomeMessage = view.findViewById(R.id.tv_welcome_message);
        textUserName = view.findViewById(R.id.tv_user_name);
        profilePicture = view.findViewById(R.id.iv_profile_picture);
        textWeeklyHours = view.findViewById(R.id.tv_weekly_hours);
        textMonthlyHours = view.findViewById(R.id.tv_monthly_hours); // <-- New
        textStatus = view.findViewById(R.id.tv_attendance_status);
        textLastCheckedIn = view.findViewById(R.id.tv_last_checked_time);
        textLeaveStatus = view.findViewById(R.id.tv_leave_status);
        statusDotView = view.findViewById(R.id.status_indicator);



        db = FirebaseFirestore.getInstance();

        btnCheckIn.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new LiveTrackingFragment());
            transaction.addToBackStack(null); // Allows user to go back
            transaction.commit();
        });

        // Set welcome message based on time
        setDynamicGreeting();

        // Fetch name from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "John Doe");
        String email = sharedPreferences.getString("email", null);

        // Set name in UI
        textUserName.setText(name);

        // Fetch work hours
        if (email != null) {
            fetchWeeklyWorkHours(email);
            fetchMonthlyWorkHours(email); // <-- New
            fetchAttendanceAndLeaveStatus(email);  // <-- Add this line



        } else {
            textWeeklyHours.setText("0h 0m");
            textMonthlyHours.setText("0h 0m");
        }


        view.findViewById(R.id.action_leave_request).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new LeaveManagementFragment())
                        .addToBackStack(null)
                        .commit()
        );

        view.findViewById(R.id.action_appraisals).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new EmployeePayrollFragment())
                        .addToBackStack(null)
                        .commit()
        );

        view.findViewById(R.id.action_reports_analytics).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ReportsFragment())
                        .addToBackStack(null)
                        .commit()
        );
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

        textWelcomeMessage.setText(greeting);
    }


    private void fetchWeeklyWorkHours(String email) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date weekEnd = cal.getTime();

        db.collection("attendance-data")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, List<DocumentSnapshot>> groupedByDate = new HashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String dateStr = doc.getString("date");
                        if (dateStr == null) continue;

                        try {
                            Date docDate = sdf.parse(dateStr);
                            if (docDate == null || docDate.before(weekStart) || docDate.after(weekEnd)) continue;

                            groupedByDate.computeIfAbsent(dateStr, k -> new ArrayList<>()).add(doc);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    long totalMinutes = calculateTotalMinutes(groupedByDate);
                    long hours = totalMinutes / 60;
                    long minutes = totalMinutes % 60;

                    textWeeklyHours.setText(hours + "h " + minutes + "m");
                })
                .addOnFailureListener(e -> textWeeklyHours.setText("0h 0m"));
    }

    private void fetchMonthlyWorkHours(String email) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date monthStart = cal.getTime();

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date monthEnd = cal.getTime();

        db.collection("attendance-data")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, List<DocumentSnapshot>> groupedByDate = new HashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String dateStr = doc.getString("date");
                        if (dateStr == null) continue;

                        try {
                            Date docDate = sdf.parse(dateStr);
                            if (docDate == null || docDate.before(monthStart) || docDate.after(monthEnd)) continue;

                            groupedByDate.computeIfAbsent(dateStr, k -> new ArrayList<>()).add(doc);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    long totalMinutes = calculateTotalMinutes(groupedByDate);
                    long hours = totalMinutes / 60;
                    long minutes = totalMinutes % 60;

                    textMonthlyHours.setText(hours + "h " + minutes + "m");
                })
                .addOnFailureListener(e -> textMonthlyHours.setText("0h 0m"));
    }

    private long calculateTotalMinutes(Map<String, List<DocumentSnapshot>> groupedByDate) {
        long totalMinutes = 0;

        for (List<DocumentSnapshot> dayEntries : groupedByDate.values()) {
            List<DocumentSnapshot> logins = new ArrayList<>();
            List<DocumentSnapshot> logouts = new ArrayList<>();

            for (DocumentSnapshot entry : dayEntries) {
                String type = entry.getString("type");
                if ("login".equalsIgnoreCase(type)) {
                    logins.add(entry);
                } else if ("logout".equalsIgnoreCase(type)) {
                    logouts.add(entry);
                }
            }

            logins.sort(Comparator.comparing(d -> d.getString("time")));
            logouts.sort(Comparator.comparing(d -> d.getString("time")));

            int pairCount = Math.min(logins.size(), logouts.size());
            for (int i = 0; i < pairCount; i++) {
                try {
                    String loginTime = logins.get(i).getString("time");
                    String logoutTime = logouts.get(i).getString("time");

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date login = timeFormat.parse(loginTime);
                    Date logout = timeFormat.parse(logoutTime);

                    if (login != null && logout != null) {
                        long diff = logout.getTime() - login.getTime();
                        totalMinutes += TimeUnit.MILLISECONDS.toMinutes(diff);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return totalMinutes;
    }


    private void fetchAttendanceAndLeaveStatus(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        db.collection("leave-requests")
                .whereEqualTo("employeeEmail", email)
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(leaveSnapshots -> {
                    boolean onLeaveToday = false;
                    String leaveRangeText = "No leave applied";

                    for (DocumentSnapshot doc : leaveSnapshots) {
                        String startDate = doc.getString("startDate");
                        String endDate = doc.getString("endDate");

                        try {
                            Date start = sdf.parse(startDate);
                            Date end = sdf.parse(endDate);
                            Date current = sdf.parse(today);

                            if (start != null && end != null && current != null &&
                                    !current.before(start) && !current.after(end)) {
                                onLeaveToday = true;
                                leaveRangeText = startDate + " - " + endDate;
                                break;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (onLeaveToday) {
                        textStatus.setText("Status: On Leave");
                        textLastCheckedIn.setText("Last Check-In: -");
                        textLeaveStatus.setText("Leave Status: " + leaveRangeText);
                        statusDotView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.yellow));
                    } else {
                        db.collection("attendance-data")
                                .whereEqualTo("email", email)
                                .whereEqualTo("date", today)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (querySnapshot.isEmpty()) {
                                        textStatus.setText("Status: Absent");
                                        textLastCheckedIn.setText("Last Check-In: -");
                                        textLeaveStatus.setText("Leave Status: No leave applied");
                                        statusDotView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.status_pending));
                                    } else {
                                        String lastCheckIn = null;

                                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                            String type = doc.getString("type");
                                            String time = doc.getString("time");
                                            if ("login".equalsIgnoreCase(type)) {
                                                if (lastCheckIn == null || time.compareTo(lastCheckIn) > 0) {
                                                    lastCheckIn = time;
                                                }
                                            }
                                        }

                                        if (lastCheckIn != null) {
                                            textStatus.setText("Status: Present");
                                            textLastCheckedIn.setText("Last Check-In: " + lastCheckIn);
                                            textLeaveStatus.setText("Leave Status: --");
                                            statusDotView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.status_approved));
                                        } else {
                                            textStatus.setText("Status: Absent");
                                            textLastCheckedIn.setText("Last Check-In: -");
                                            textLeaveStatus.setText("Leave Status: No leave applied");
                                            statusDotView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.status_pending));
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    textStatus.setText("Status: Absent");
                                    textLastCheckedIn.setText("Last Check-In: -");
                                    textLeaveStatus.setText("Leave Status: No leave applied");
                                    statusDotView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.status_pending));
                                });
                    }

                })
                .addOnFailureListener(e -> {
                    textStatus.setText("Status: Absent");
                    textLastCheckedIn.setText("Last Check-In: -");
                    textLeaveStatus.setText("Leave Status: No leave applied");
                    statusDotView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.status_pending));
                });
    }


//    private void fetchAttendanceAndLeaveStatus(String email) {
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // 1. Check today's attendance
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        String today = sdf.format(new Date());
//
//        db.collection("attendance-data")
//                .whereEqualTo("email", email)
//                .whereEqualTo("date", today)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    if (querySnapshot.isEmpty()) {
//                        textStatus.setText("Status: Absent");
//                        textLastCheckedIn.setText("Last Check-In: -");
//                    } else {
//                        List<DocumentSnapshot> docs = querySnapshot.getDocuments();
//                        String lastCheckIn = null;
//
//                        for (DocumentSnapshot doc : docs) {
//                            String type = doc.getString("type");
//                            String time = doc.getString("time");
//                            if ("login".equalsIgnoreCase(type)) {
//                                if (lastCheckIn == null || time.compareTo(lastCheckIn) > 0) {
//                                    lastCheckIn = time;
//                                }
//                            }
//                        }
//
//                        if (lastCheckIn != null) {
//                            textStatus.setText("Status: Present");
//                            textLastCheckedIn.setText("Last Check-In: " + lastCheckIn);
//                        } else {
//                            textStatus.setText("Status: Absent");
//                            textLastCheckedIn.setText("Last Check-In: -");
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    textStatus.setText("Status: Absent");
//                    textLastCheckedIn.setText("Last Check-In: -");
//                });
//
//        // 2. Get latest leave request
//        db.collection("leave-requests")
//                .whereEqualTo("employeeEmail", email)
//                .orderBy("startDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
//                .limit(1)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    if (querySnapshot.isEmpty()) {
//                        textLeaveStatus.setText("Leave Status: No leave applied");
//                    } else {
//                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
//                        String status = doc.getString("status");
//                        if (status != null && !status.isEmpty()) {
//                            textLeaveStatus.setText("Leave Status: " + capitalizeFirstLetter(status));
//                        } else {
//                            textLeaveStatus.setText("Leave Status: No leave applied");
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> textLeaveStatus.setText("Leave Status: No leave applied"));
//    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }


}