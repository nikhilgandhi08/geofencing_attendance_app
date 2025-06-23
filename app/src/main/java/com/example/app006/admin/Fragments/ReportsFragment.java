package com.example.app006.admin.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.app006.R;


import com.example.app006.adapters.LeaderboardAdapter;
import com.example.app006.adapters.LeaveReportAdapter;
import com.example.app006.models.LeaderboardEmployee;
import com.example.app006.models.LeaveRequest;
import com.example.app006.models.LeaveRequestNew;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import android.widget.FrameLayout;
import android.widget.Toast;


public class ReportsFragment extends Fragment {

    private static final String TAG = "ReportsFragment";

    private FirebaseFirestore db;
    private TextView tvAttendanceRate, tvPunctualityRate ;
    private String currentAdminEmail;

    private BarChart attendanceBarChart;
    private TextView barChartPlaceholder;

    private FrameLayout scatterChartContainer;
    private ScatterChart scatterChart;
    private TextView tvPendingCount, tvApprovedCount, tvRejectedCount;

    private RecyclerView rvLeaderboard;
    private MaterialButton btnShowAllEmployees;
    private boolean showAll = false;
    private List<LeaderboardEmployee> leaderboardList = new ArrayList<>();

    private List<LeaderboardEmployee> allEmployees = new ArrayList<>();
    private LeaderboardAdapter adapter;

    private MaterialButton btnViewLeaveDetails;
    private RecyclerView rvLeaveReport;
    private LeaveReportAdapter leaveReportAdapter;
    private List<LeaveRequestNew> leaveRequestList = new ArrayList<>();




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        tvAttendanceRate = view.findViewById(R.id.tvAttendanceRate);
        tvPunctualityRate = view.findViewById(R.id.tvPunctualityRate);

        attendanceBarChart = view.findViewById(R.id.attendanceBarChart);
        barChartPlaceholder = view.findViewById(R.id.barChartPlaceholder);
        scatterChartContainer = view.findViewById(R.id.scatterChartContainer);

        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        tvApprovedCount = view.findViewById(R.id.tvApprovedCount);
        tvRejectedCount = view.findViewById(R.id.tvRejectedCount);

        rvLeaderboard = view.findViewById(R.id.rvLeaderboard);
        btnShowAllEmployees = view.findViewById(R.id.btnShowAllEmployees);

        adapter = new LeaderboardAdapter(leaderboardList, getContext());
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLeaderboard.setAdapter(adapter);

        MaterialButton btnShowAll = view.findViewById(R.id.btnShowAllEmployees);

        // Leave Report Section
        btnViewLeaveDetails = view.findViewById(R.id.btnViewLeaveDetails);
        rvLeaveReport = view.findViewById(R.id.rvLeaveReport);
        leaveReportAdapter = new LeaveReportAdapter(leaveRequestList, getContext());
        rvLeaveReport.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLeaveReport.setAdapter(leaveReportAdapter);
        rvLeaveReport.setVisibility(View.GONE); // Initially hidden

        btnViewLeaveDetails.setOnClickListener(v -> {
            if (rvLeaveReport.getVisibility() == View.VISIBLE) {
                rvLeaveReport.setVisibility(View.GONE);
                btnViewLeaveDetails.setText("View Detailed Leave Report");
            } else {
                fetchLeaveRequests();
            }
        });

        MaterialButton btnRefresh = view.findViewById(R.id.btnRefresh);

        btnRefresh.setOnClickListener(v -> {
            btnRefresh.animate().rotationBy(360f).setDuration(600).start();
            Toast.makeText(getContext(), "Refreshing Report...", Toast.LENGTH_SHORT).show();
            refreshReportData();
        });


        // Get admin email from SharedPreferences (like dashboard)
        SharedPreferences prefs = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        currentAdminEmail = prefs.getString("email", null);



        if (currentAdminEmail != null) {
            fetchKPIData();

        } else {
            Log.e(TAG, "Admin email not found in SharedPreferences");
        }

        btnShowAllEmployees.setOnClickListener(v -> {
            showAll = !showAll;
            btnShowAllEmployees.setText(showAll ? "Show Top 5" : "Show All Employees");
            updateLeaderboardDisplay();
        });

        if (currentAdminEmail != null) {
            fetchLeaderboardData(); // loads once, then updates via toggle
        }


    }
    private void fetchKPIData() {
        // Get all employees linked to this admin
        db.collection("hr-employees")
                .whereEqualTo("adminEmail", currentAdminEmail)
                .get()
                .addOnSuccessListener(hrSnapshot -> {
                    List<String> employeeEmails = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : hrSnapshot) {
                        String empEmail = doc.getString("empEmail");
                        if (empEmail != null) employeeEmails.add(empEmail);
                    }

                    if (!employeeEmails.isEmpty()) {
                        fetchAttendanceAndPunctuality(employeeEmails);
                        loadWeeklyAttendanceOverview(employeeEmails);
                        loadLoginTimeScatterPlot(employeeEmails);  // << Add this line
                        fetchLeaveRequestStatusCounts(employeeEmails);

                    } else {
                        Log.d(TAG, "No employees linked to admin: " + currentAdminEmail);
                        // Reset KPIs to zero or empty
                        tvAttendanceRate.setText("0%");
                        tvPunctualityRate.setText("0%");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch employees", e));
    }

    private void fetchAttendanceAndPunctuality(List<String> employeeEmails) {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("attendance-data")
                .whereEqualTo("date", todayDate)
                .get()
                .addOnSuccessListener(attendanceSnapshot -> {
                    Set<String> uniqueLogins = new HashSet<>();
                    Set<String> punctualLogins = new HashSet<>();

                    for (QueryDocumentSnapshot doc : attendanceSnapshot) {
                        String email = doc.getString("email");
                        String type = doc.getString("type");
                        String time = doc.getString("time");

                        if (email == null || type == null || !employeeEmails.contains(email)) continue;

                        if ("login".equalsIgnoreCase(type)) {
                            uniqueLogins.add(email);  // Only count once per employee

                            // Punctual if logged in by 10:00 AM
                            if (time != null && time.compareTo("10:00") <= 0) {
                                punctualLogins.add(email);
                            }
                        }
                    }

                    int totalEmployees = employeeEmails.size();
                    int presentToday = uniqueLogins.size();
                    int punctualToday = punctualLogins.size();

                    double attendanceRate = totalEmployees > 0 ? (presentToday * 100.0 / totalEmployees) : 0;
                    double punctualityRate = presentToday > 0 ? (punctualToday * 100.0 / presentToday) : 0;

                    tvAttendanceRate.setText(String.format(Locale.getDefault(), "%.1f%%", attendanceRate));
                    tvPunctualityRate.setText(String.format(Locale.getDefault(), "%.1f%%", punctualityRate));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch attendance data", e));
    }


    private void loadWeeklyAttendanceOverview(List<String> employeeEmails) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Map<String, Set<String>> dateToUniqueLogins = new LinkedHashMap<>();

        // Initialize each date with an empty set
        for (int i = 6; i >= 0; i--) {
            Calendar temp = (Calendar) calendar.clone();
            temp.add(Calendar.DATE, -i);
            String date = sdf.format(temp.getTime());
            dateToUniqueLogins.put(date, new HashSet<>());
        }

        db.collection("attendance-data")
                .whereIn("email", employeeEmails)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String date = doc.getString("date");
                        String type = doc.getString("type");
                        String email = doc.getString("email");

                        if (date != null && email != null && "login".equalsIgnoreCase(type) && dateToUniqueLogins.containsKey(date)) {
                            dateToUniqueLogins.get(date).add(email);  // Set prevents duplicates
                        }
                    }

                    // Convert sets to integer counts
                    Map<String, Integer> dateToLoginCount = new LinkedHashMap<>();
                    for (Map.Entry<String, Set<String>> entry : dateToUniqueLogins.entrySet()) {
                        dateToLoginCount.put(entry.getKey(), entry.getValue().size());
                    }

                    updateBarChart(dateToLoginCount);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load weekly attendance", e));
    }


    private void loadLoginTimeScatterPlot(List<String> employeeEmails) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Map date string to X-axis index
        Map<String, Integer> dateToIndex = new LinkedHashMap<>();
        // Store labels for X axis
        final List<String> xLabels = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            Calendar temp = (Calendar) calendar.clone();
            temp.add(Calendar.DATE, -i);
            String dateStr = sdf.format(temp.getTime());
            dateToIndex.put(dateStr, 6 - i); // 0..6
            try {
                Date date = sdf.parse(dateStr);
                String label = new SimpleDateFormat("dd-MM", Locale.getDefault()).format(date);
                xLabels.add(label);
            } catch (Exception e) {
                xLabels.add(dateStr);
            }
        }

        // Fetch attendance login data for these employees for last 7 days
        db.collection("attendance-data")
                .whereIn("email", employeeEmails)
                .whereEqualTo("type", "login")
                .get()
                .addOnSuccessListener(snapshot -> {
                    // Map email -> List<Entry> for ScatterDataSet
                    Map<String, List<Entry>> employeeEntries = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String email = doc.getString("email");
                        String date = doc.getString("date");
                        String timeStr = doc.getString("time");

                        if (email == null || date == null || timeStr == null) continue;

                        if (!dateToIndex.containsKey(date)) continue; // Only last 7 days

                        int xIndex = dateToIndex.get(date);
                        float yValue = timeStringToMinutes(timeStr);

                        // Store entries by employee email
                        employeeEntries.putIfAbsent(email, new ArrayList<>());
                        employeeEntries.get(email).add(new Entry(xIndex, yValue));
                    }

                    // Prepare ScatterDataSets with unique colors
                    List<IScatterDataSet> dataSets = new ArrayList<>();

                    // Map email to username for legend
                    Map<String, String> emailToUsername = new HashMap<>();

                    // Fetch usernames for emails (optional: you can skip this or assume email as label)
                    db.collection("users")
                            .whereIn("email", employeeEmails)
                            .get()
                            .addOnSuccessListener(userSnap -> {
                                for (QueryDocumentSnapshot userDoc : userSnap) {
                                    String email = userDoc.getString("email");
                                    String username = userDoc.getString("username");
                                    if (email != null && username != null) {
                                        emailToUsername.put(email, username);
                                    }
                                }

                                // Generate colors: use predefined colors or random distinct colors
                                int[] availableColors = ColorTemplate.MATERIAL_COLORS; // Or VORDIPLOM_COLORS or your own list
                                int colorCount = availableColors.length;

                                int colorIdx = 0;

                                for (Map.Entry<String, List<Entry>> entry : employeeEntries.entrySet()) {
                                    String email = entry.getKey();
                                    List<Entry> entries = entry.getValue();

                                    ScatterDataSet dataSet = new ScatterDataSet(entries, emailToUsername.getOrDefault(email, email));
                                    int color = availableColors[colorIdx % colorCount];
                                    dataSet.setColor(color);
                                    dataSet.setScatterShapeSize(10f);
                                    dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
                                    dataSet.setDrawValues(false);

                                    dataSets.add(dataSet);
                                    colorIdx++;
                                }

                                // Remove old chart if exists
                                scatterChartContainer.removeAllViews();

                                // Create ScatterChart and add to container
                                scatterChart = new ScatterChart(requireContext());
                                scatterChartContainer.addView(scatterChart, new FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                                ScatterData scatterData = new ScatterData(dataSets);
                                scatterChart.setData(scatterData);

                                // Configure chart
                                scatterChart.getDescription().setEnabled(false);
                                scatterChart.getLegend().setEnabled(true);
                                scatterChart.setPinchZoom(true);
                                scatterChart.setScaleEnabled(true);

                                // X Axis
                                XAxis xAxis = scatterChart.getXAxis();
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                xAxis.setGranularity(1f);
                                xAxis.setDrawGridLines(false);
                                xAxis.setLabelRotationAngle(0);
                                xAxis.setValueFormatter(new ValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value) {
                                        int index = (int) value;
                                        if (index >= 0 && index < xLabels.size()) {
                                            return xLabels.get(index);
                                        }
                                        return "";
                                    }
                                });

                                // Y Axis - minutes converted to "HH:mm" for labels
                                scatterChart.getAxisRight().setEnabled(false);
                                YAxis yAxisLeft = scatterChart.getAxisLeft();
                                yAxisLeft.setGranularity(30f); // 30 minutes granularity approx.
                                yAxisLeft.setDrawGridLines(true);
                                yAxisLeft.setValueFormatter(new ValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value) {
                                        return minutesToTimeString((int) value);
                                    }
                                });
                                yAxisLeft.setAxisMinimum(480f); // 8:00 AM in minutes
                                yAxisLeft.setAxisMaximum(720f); // 12:00 PM in minutes
                                yAxisLeft.setLabelCount(5, true); // e.g., 8:00, 9:00, 10:00, 11:00, 12:00


                                scatterChart.invalidate(); // refresh
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to fetch user data for scatter plot", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch attendance data for scatter plot", e);
                });
    }

    // Convert "HH:mm" string to total minutes from midnight
    private float timeStringToMinutes(String time) {
        try {
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (Exception e) {
            return 0f;
        }
    }

    // Convert total minutes to "HH:mm" string
    private String minutesToTimeString(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }



    private void updateBarChart(Map<String, Integer> loginData) {
        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Integer> entry : loginData.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(entry.getKey());
                String formatted = new SimpleDateFormat("dd-MM", Locale.getDefault()).format(date);
                labels.add(formatted);
            } catch (Exception e) {
                labels.add(entry.getKey()); // fallback
            }

            index++;
        }

        if (entries.isEmpty()) {
            attendanceBarChart.setVisibility(View.GONE);
            barChartPlaceholder.setVisibility(View.VISIBLE);
            return;
        } else {
            attendanceBarChart.setVisibility(View.VISIBLE);
            barChartPlaceholder.setVisibility(View.GONE);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Logins");
        dataSet.setColor(getResources().getColor(R.color.primary_blue, null));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);

        attendanceBarChart.setData(barData);
        attendanceBarChart.getDescription().setEnabled(false);
        attendanceBarChart.getLegend().setEnabled(false);
        attendanceBarChart.setScaleEnabled(false);
        attendanceBarChart.setPinchZoom(false);
        attendanceBarChart.setDrawGridBackground(false);
        attendanceBarChart.setFitBars(true);
        attendanceBarChart.animateY(700);

        // X-Axis
        XAxis xAxis = attendanceBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < labels.size()) {
                    return labels.get((int) value);
                }
                return "";
            }
        });

        // Y-Axis
        YAxis leftAxis = attendanceBarChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);

        attendanceBarChart.getAxisRight().setEnabled(false);
        attendanceBarChart.invalidate(); // refresh
    }

    private void fetchLeaveRequestStatusCounts(List<String> employeeEmails) {
        if (employeeEmails.isEmpty()) {
            tvPendingCount.setText("0");
            tvApprovedCount.setText("0");
            tvRejectedCount.setText("0");
            return;
        }

        db.collection("leave-requests")
                .whereIn("employeeEmail", employeeEmails)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int pendingCount = 0;
                    int approvedCount = 0;
                    int rejectedCount = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("status");
                        if (status != null) {
                            switch (status.toLowerCase()) {
                                case "pending":
                                    pendingCount++;
                                    break;
                                case "approved":
                                    approvedCount++;
                                    break;
                                case "rejected":
                                    rejectedCount++;
                                    break;
                            }
                        }
                    }

                    // Update UI
                    tvPendingCount.setText(String.valueOf(pendingCount));
                    tvApprovedCount.setText(String.valueOf(approvedCount));
                    tvRejectedCount.setText(String.valueOf(rejectedCount));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch leave requests", e);
                    // Set to zero on failure to avoid confusion
                    tvPendingCount.setText("0");
                    tvApprovedCount.setText("0");
                    tvRejectedCount.setText("0");
                });
    }


    private void fetchLeaveRequests() {
        db.collection("hr-employees")
                .whereEqualTo("adminEmail", currentAdminEmail)
                .get()
                .addOnSuccessListener(hrSnapshot -> {
                    List<String> empEmails = new ArrayList<>();
                    for (DocumentSnapshot doc : hrSnapshot) {
                        empEmails.add(doc.getString("empEmail"));
                    }

                    if (empEmails.isEmpty()) {
                        rvLeaveReport.setVisibility(View.GONE);
                        btnViewLeaveDetails.setText("No Leave Requests");
                        return;
                    }

                    db.collection("leave-requests")
                            .whereIn("employeeEmail", empEmails)
                            .get()
                            .addOnSuccessListener(leaveSnapshot -> {
                                leaveRequestList.clear();
                                Map<String, String> emailToNameMap = new HashMap<>();
                                Set<String> leaveEmpEmails = new HashSet<>();

                                for (DocumentSnapshot leaveDoc : leaveSnapshot) {
                                    leaveEmpEmails.add(leaveDoc.getString("employeeEmail"));
                                }

                                if (leaveEmpEmails.isEmpty()) {
                                    rvLeaveReport.setVisibility(View.GONE);
                                    btnViewLeaveDetails.setText("No Leave Requests");
                                    return;
                                }

                                db.collection("users")
                                        .whereIn("email", new ArrayList<>(leaveEmpEmails))
                                        .get()
                                        .addOnSuccessListener(userSnapshot -> {
                                            for (DocumentSnapshot userDoc : userSnapshot) {
                                                emailToNameMap.put(userDoc.getString("email"), userDoc.getString("name"));
                                            }

                                            for (DocumentSnapshot leaveDoc : leaveSnapshot) {
                                                String email = leaveDoc.getString("employeeEmail");
                                                String employeeName = emailToNameMap.getOrDefault(email, "Unknown");
                                                String reason = leaveDoc.getString("reason");
                                                String startDate = leaveDoc.getString("startDate");
                                                String endDate = leaveDoc.getString("endDate");
                                                String status = leaveDoc.getString("status");

                                                LeaveRequestNew leaveRequest = new LeaveRequestNew(employeeName, reason, startDate, endDate, status);
                                                leaveRequestList.add(leaveRequest);
                                            }

                                            if (leaveRequestList.isEmpty()) {
                                                rvLeaveReport.setVisibility(View.GONE);
                                                btnViewLeaveDetails.setText("No Leave Requests");
                                            } else {
                                                rvLeaveReport.setVisibility(View.VISIBLE);
                                                btnViewLeaveDetails.setText("Hide Detailed Leave Report");
                                                leaveReportAdapter.notifyDataSetChanged();
                                            }
                                        });
                            });
                });
    }

    private void fetchLeaderboardData() {
        db.collection("hr-employees")
                .whereEqualTo("adminEmail", currentAdminEmail)
                .get()
                .addOnSuccessListener(hrSnapshot -> {
                    List<String> empEmails = new ArrayList<>();
                    for (DocumentSnapshot doc : hrSnapshot) {
                        empEmails.add(doc.getString("empEmail"));
                    }

                    if (empEmails.isEmpty()) return;

                    String currentMonthPrefix = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
                    Map<String, Integer> loginCountMap = new HashMap<>();

                    db.collection("attendance-data")
                            .whereIn("email", empEmails)
                            .get()
                            .addOnSuccessListener(attSnap -> {
                                for (DocumentSnapshot doc : attSnap) {
                                    String date = doc.getString("date");
                                    String type = doc.getString("type");
                                    String email = doc.getString("email");

                                    if (type != null && type.equals("login") &&
                                            date != null && date.startsWith(currentMonthPrefix)) {
                                        loginCountMap.put(email, loginCountMap.getOrDefault(email, 0) + 1);
                                    }
                                }

                                db.collection("users")
                                        .whereIn("email", new ArrayList<>(loginCountMap.keySet()))
                                        .get()
                                        .addOnSuccessListener(userSnap -> {
                                            allEmployees.clear();

                                            for (DocumentSnapshot userDoc : userSnap) {
                                                String name = userDoc.getString("name");
                                                String email = userDoc.getString("email");
                                                int count = loginCountMap.getOrDefault(email, 0);

                                                allEmployees.add(new LeaderboardEmployee(name, email, count));
                                            }

                                            // Sort all employees by login count descending
                                            Collections.sort(allEmployees, (a, b) -> Integer.compare(b.getLoginCount(), a.getLoginCount()));

                                            updateLeaderboardDisplay();
                                        });
                            });
                });
    }


    private void updateLeaderboardDisplay() {
        List<LeaderboardEmployee> displayList = new ArrayList<>();

        if (showAll || allEmployees.size() <= 5) {
            displayList.addAll(allEmployees);
        } else {
            displayList.addAll(allEmployees.subList(0, 5));
        }

        adapter = new LeaderboardAdapter(displayList, getContext());
        rvLeaderboard.setAdapter(adapter);
    }


    private void refreshReportData() {
        fetchKPIData();
        fetchLeaderboardData();
    }




}
